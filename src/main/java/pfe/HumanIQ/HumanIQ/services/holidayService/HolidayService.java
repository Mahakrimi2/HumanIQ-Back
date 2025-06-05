package pfe.HumanIQ.HumanIQ.services.holidayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailDetails;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
import pfe.HumanIQ.HumanIQ.models.Holiday;
import pfe.HumanIQ.HumanIQ.models.HolidayStatus;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.HolidayRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.Notifications.NotificationService;

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

    @Autowired
    private NotificationService notificationService;



    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    private UserRepo userRepo;

    public Holiday createHolidayRequest(Holiday holiday) throws IOException {
        holiday.setStatus(HolidayStatus.PENDING);
        Holiday savedHoliday = holidayRepository.save(holiday);
        notificationService.notifyAboutHolidayRequest(savedHoliday);
        return savedHoliday;
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


    public String updateHolidayStatus(Long id, HolidayStatus status) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found with id: " + id));

        User user = holiday.getUser();
        System.out.println(status);
        if (status == HolidayStatus.ACCEPTED ) {
            int balance = user.getLeave_balance();
            int duration = holiday.getDuration();

            if (balance >= duration && balance > 0) {
                user.setLeave_balance(balance - duration);
                userRepo.save(user);
            } else {
                return "Insufficient leave balance. Holiday cannot be accepted.";
            }

        } else if (status == HolidayStatus.CANCELLED) {
            user.setLeave_balance(user.getLeave_balance());
            userRepo.save(user);

        }

        holiday.setStatus(status);
        Holiday updatedHoliday = holidayRepository.save(holiday);
        String subject = "Your Holiday Request Update";
        // Message HTML avec style moderne
        String htmlMessage = """
    <html>
        <head>
            <style>
                body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 20px auto; padding: 20px; }
                .header { color: #2c3e50; border-bottom: 2px solid #f39c12; padding-bottom: 10px; }
                .status-accepted { color: #27ae60; font-weight: bold; }
                .status-rejected { color: #e74c3c; font-weight: bold; }
                .status-pending { color: #f39c12; font-weight: bold; }
                .details { background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0; }
                .footer { margin-top: 20px; font-size: 0.9em; color: #7f8c8d; }
                .emoji { font-size: 1.2em; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2>Holiday Request Update</h2>
                </div>
                <p>Dear %s,</p>
                
                <p>Here is the status of your holiday request:</p>
                
                <div class="details">
                    <p><span class="emoji">📅</span> <strong>Created At:</strong> %s</p>
                    <p><span class="emoji">📌</span> <strong>Status:</strong> <span class="status-%s">%s</span></p>
                    <p><span class="emoji">⏱️</span> <strong>Duration:</strong> %s days</p>
                </div>
    """.formatted(
                user.getFullname(),
                updatedHoliday.getCreatedAt(),
                status.name().toLowerCase(),
                status.name(),
                updatedHoliday.getDuration()
        );

        // Ajout du message spécifique au statut
        if (status == HolidayStatus.ACCEPTED) {
            htmlMessage += """
        <div style="background: #e8f5e9; padding: 10px; border-radius: 5px; margin: 15px 0;">
            <p><span class="emoji">✅</span> Your holiday request has been <strong>ACCEPTED</strong>.</p>
            <p>Enjoy your time off!</p>
        </div>
        """;
        }  else if (status == HolidayStatus.CANCELLED) {
            htmlMessage += """
        <div style="background: #fff8e1; padding: 10px; border-radius: 5px; margin: 15px 0;">
            <p><span class="emoji">🔄</span> Your holiday request has been <strong>CANCELLED</strong>.</p>
            <p>Your leave balance has been restored.</p>
        </div>
        """;
        }

// Footer du message
        htmlMessage += """
        <div class="footer">
            <p>Best regards,</p>
            <p><strong>HR Department</strong></p>
            <p>STE Resco Devloppment</p>
        </div>
    </div>
    </body>
    </html>
    """;

        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getUsername());
        details.setSubject(subject);
        details.setMsgBody(htmlMessage);

        emailService.sendSimpleMail(details);


        String smsMessage = "Hello " + user.getUsername() + ", your holiday request has been " + updatedHoliday.getStatus() + ".";

//        if (user.getTelNumber() != null) {
//            smsService.sendSms(user.getTelNumber(), smsMessage);
//        }
        return "updated";
    }

    public List<Holiday> getHolidaysByEmpUsername(String username) {
        return holidayRepository.findByUsername(username);
    }


}