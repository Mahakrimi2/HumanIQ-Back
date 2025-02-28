package pfe.HumanIQ.HumanIQ.emailConfig;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import pfe.HumanIQ.HumanIQ.models.User;

import static javax.swing.UIManager.put;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final String sender;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String sender) {
        this.javaMailSender = javaMailSender;
        this.sender = sender;
    }

    @Override
    public String sendMailWithAttachment(EmailDetails details, MultipartFile attachment) {
        if (details.getRecipient() == null || details.getMsgBody() == null || details.getSubject() == null || attachment == null) {
            throw new IllegalArgumentException("Error: Recipient, message body, subject, and attachment cannot be null.");
        }

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody(),true);
            mimeMessageHelper.setSubject(details.getSubject());

            mimeMessageHelper.addAttachment(attachment.getOriginalFilename(), attachment);

            javaMailSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        } catch (MessagingException e) {
            return "Error while Sending Mail with Attachment: " + e.getMessage();
        }
    }

    @Override
    public String sendSimpleMail(EmailDetails details) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, false);  // False because no attachment
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody(), true);  // true means the body can contain HTML
            mimeMessageHelper.setSubject(details.getSubject());

            javaMailSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        } catch (MessagingException e) {
            return "Error while Sending Simple Mail: " + e.getMessage();
        }
    }



    public void sendPasswordResetEmail(String recipient, String token) {
        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        String message = "Cliquez sur le lien suivant pour réinitialiser votre mot de passe : <a href='" + resetLink + "'>Réinitialiser mon mot de passe</a>";

        EmailDetails details = new EmailDetails();
        details.setRecipient(recipient);
        details.setSubject("Réinitialisation de votre mot de passe");
        details.setMsgBody(message);

        sendSimpleMail(details);
    }
    public void buildVerificationUrl(final String baseURL, final String token){
        final String url= UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/register/verify").queryParam("token", token).toUriString();
        put("verificationURL", url);
    }


    // Méthode pour envoyer un email de vérification
    public void sendVerificationEmail(String recipient, String token) {
        String subject = "Account verification";  // Correct subject name

        // Message contenant le code de vérification directement
        String message = "<html><body>" +
                "<p>Votre code de vérification est :</p>" +
                "<h2 style='color: blue;'>" + token + "</h2>" +
                "<p>Ce code expirera dans 24 heures.</p>" +
                "</body></html>";

        // Création de l'email
        EmailDetails details = new EmailDetails();
        details.setRecipient(recipient);
        details.setSubject(subject);
        details.setMsgBody(message);

        // Envoi de l'email
        sendSimpleMail(details);
    }

}