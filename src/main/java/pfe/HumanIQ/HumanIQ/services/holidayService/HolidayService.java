package pfe.HumanIQ.HumanIQ.services.holidayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.Holiday;
import pfe.HumanIQ.HumanIQ.repositories.HolidayRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;



    @Value("${file.upload-dir}")
    private String uploadDir;

    public Holiday createHolidayRequest(Holiday holiday, MultipartFile certificate) throws IOException {
        if (holiday.getType() == null || holiday.getStartDate() == null || holiday.getDuration() <= 0 || holiday.getReason() == null || holiday.getReason().isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        String certificatePath = null;
        if (certificate != null && !certificate.isEmpty()) {
            certificatePath = saveCertificate(certificate);
        }

        holiday.setStatus("PENDING");
        holiday.setCertificate(certificatePath);
        return holidayRepository.save(holiday);
    }

    public Holiday approveHoliday(Long id, String approvedBy) {
        return holidayRepository.findById(id)
                .map(holiday -> {
                    holiday.setStatus("APPROVED");
                    return holidayRepository.save(holiday);
                })
                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));
    }


    public Holiday rejectHoliday(Long id, String rejectedBy) {
        return holidayRepository.findById(id)
                .map(holiday -> {
                    holiday.setStatus("REJECTED");
                    return holidayRepository.save(holiday);
                })
                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));
    }


    public void deleteHoliday(Long id) {
        holidayRepository.findById(id).ifPresent(holiday -> {
            if (holiday.getCertificate() != null) {
                try {
                    Path filePath = Paths.get(uploadDir, holiday.getCertificate());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete certificate file", e);
                }
            }
            holidayRepository.deleteById(id);
        });
    }


    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
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
}