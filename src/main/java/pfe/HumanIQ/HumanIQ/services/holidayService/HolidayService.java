package pfe.HumanIQ.HumanIQ.services.holidayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailDetails;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
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

    @Autowired

    private EmailService emailService;



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
            if (holiday.getFicher() != null) {
                try {
                    Path filePath = Paths.get(uploadDir, holiday.getFicher());
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

        User user = holiday.getUser();
        if (status == HolidayStatus.ACCEPTED) {
            int number=user.getLeave_balance() - holiday.getDuration();
           user.setLeave_balance(number);
            userRepo.save(user);
        } else if (status == HolidayStatus.CANCELLED) {
            user.setLeave_balance(user.getLeave_balance());
            userRepo.save(user);
        }

        holiday.setStatus(status);
        Holiday updatedHoliday = holidayRepository.save(holiday);

        String subject = "Your Holiday Request Update";
        String message = "Dear " + user.getUsername() + ",\n\n"
                + "Here is the status of your holiday request:\n"
                + "📅 Created At: " + updatedHoliday.getCreatedAt() + "\n"
                + "📌 Status: " + updatedHoliday.getStatus() + "\n\n";

        if (status == HolidayStatus.ACCEPTED) {
            message += "✅ Your holiday request has been *ACCEPTED* for " + updatedHoliday.getDuration() + " days.\n";
        } else if (status == HolidayStatus.CANCELLED) {
            message += "❌ Your holiday request has been *CANCELLED* and your leave balance has been restored.\n";
        }

        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getUsername());
        details.setSubject(subject);
        details.setMsgBody(message);
        emailService.sendSimpleMail(details);

        String smsMessage = "Hello " + user.getUsername() + ", your holiday request has been " + updatedHoliday.getStatus() + ".";

//        if (user.getTelNumber() != null) {
//            smsService.sendSms(user.getTelNumber(), smsMessage);
//        }
        return updatedHoliday;
    }

    public List<Holiday> getHolidaysByEmpUsername(String username) {
        return holidayRepository.findByUsername(username);
    }


}