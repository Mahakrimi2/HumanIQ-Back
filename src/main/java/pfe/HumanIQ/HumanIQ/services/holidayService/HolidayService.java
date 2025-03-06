package pfe.HumanIQ.HumanIQ.services.holidayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.Holiday;
import pfe.HumanIQ.HumanIQ.models.HolidayStatus;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.HolidayRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    private UserRepo userRepo;

    public Holiday createHolidayRequest(Holiday holiday) throws IOException {
        holiday.setStatus(HolidayStatus.PENDING);
        return holidayRepository.save(holiday);
    }

//    public Holiday approveHoliday(Long id, String approvedBy) {
//        return holidayRepository.findById(id)
//                .map(holiday -> {
//                    holiday.setStatus("APPROVED");
//                    return holidayRepository.save(holiday);
//                })
//                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));
//    }
//
//
//    public Holiday rejectHoliday(Long id, String rejectedBy) {
//        return holidayRepository.findById(id)
//                .map(holiday -> {
//                    holiday.setStatus("REJECTED");
//                    return holidayRepository.save(holiday);
//                })
//                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));
//    }
//

    public void deleteHoliday(Long id) {
        holidayRepository.findById(id).ifPresent(holiday -> {
            if (holiday.getFile() != null) {
                try {
                    Path filePath = Paths.get(uploadDir, holiday.getFile());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete certificate file", e);
                }
            }
            holidayRepository.deleteById(id);
        });
    }


    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAllWithUser();
    }


    public Optional<Holiday> getHolidayById(Long id) {
        return holidayRepository.findById(id);
    }

    private String saveCertificate(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        return fileName;
    }


    public Holiday updateHolidayStatus(Long id, HolidayStatus status) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));

        if (status == HolidayStatus.ACCEPTED) {
            User user = holiday.getUser();
            user.setLeave_balance(user.getLeave_balance() - holiday.getDuration());
            userRepo.save(user);
        }

        holiday.setStatus(status);
        return holidayRepository.save(holiday);
    }
    public List<Holiday> getHolidaysByEmpUsername(String username) {
        return holidayRepository.findByUsername(username);
    }


}