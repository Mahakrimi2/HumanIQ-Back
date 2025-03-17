package pfe.HumanIQ.HumanIQ.emailConfig;

import org.springframework.web.multipart.MultipartFile;

public interface EmailService {

    String sendSimpleMail(EmailDetails details);
    public boolean sendfichedepaie(String recipient, String title, byte[] body);

    String sendMailWithAttachment(EmailDetails details, MultipartFile attachment);

    void sendVerificationEmail(String email, String token);
}
