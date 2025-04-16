package pfe.HumanIQ.HumanIQ.emailConfig;

import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import static javax.swing.UIManager.put;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final String sender;
    private final JavaMailSenderImpl mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String sender, JavaMailSenderImpl mailSender) {
        this.javaMailSender = javaMailSender;
        this.sender = sender;
        this.mailSender = mailSender;
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
            mimeMessageHelper.setText(details.getMsgBody(), true);
            mimeMessageHelper.setSubject(details.getSubject());

            mimeMessageHelper.addAttachment(attachment.getOriginalFilename(), attachment);

            javaMailSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        } catch (MessagingException e) {
            return "Error while Sending Mail with Attachment: " + e.getMessage();
        }
    }

    @Override
    public void sendVerificationEmail(String email, String token) {

    }

    @Override
    public String sendSimpleMail(EmailDetails details) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, false);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody(), true);
            mimeMessageHelper.setSubject(details.getSubject());

            javaMailSender.send(mimeMessage);
            return "Mail Sent Successfully...";
        } catch (MessagingException e) {
            return "Error while Sending Simple Mail: " + e.getMessage();
        }
    }

    @Override
    public boolean sendfichedepaie(String recipient, String title, byte[] body) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(recipient);
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setText("Bonjour,\n\nVeuillez trouver ci-joint votre fiche de paie pour ce mois.\n\nCordialement.");

            // tbadelik mel byte lil pdf
            DataSource dataSource = new ByteArrayDataSource(body, "application/pdf");
            mimeMessageHelper.addAttachment("Fiche_de_paie.pdf", dataSource);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    public void sendPasswordResetEmail(String recipient, String encryptedPassword) throws MessagingException {
        String htmlContent = buildPasswordEmailContent(encryptedPassword);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(sender);
        helper.setTo(recipient);
        helper.setSubject("Votre nouveau mot de passe est prêt");
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private String buildPasswordEmailContent(String encryptedPassword) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <title>Your New Password</title>"
                + "    <style>"
                + "        body { font-family: Arial, sans-serif; }"
                + "        .container { max-width: 600px; margin: auto; padding: 20px; }"
                + "        .info-box { background: #f8f9fa; padding: 15px; border-radius: 5px; }"
                + "        .login-btn { "
                + "            display: inline-block; "
                + "            padding: 10px 20px; "
                + "            background: #4CAF50; "
                + "            color: white; "
                + "            text-decoration: none; "
                + "            border-radius: 5px; "
                + "        }"
                + "        code { word-break: break-all; }"
                + "    </style>"
                + "</head>"
                + "<body>"
                + "    <div class='container'>"
                + "        <h2>Your Password Has Been Reset</h2>"
                + "        <div class='info-box'>"
                + "            <p>A new secure password has been generated and encrypted for you.</p>"
                + "            <p><strong>Encrypted Password:</strong></p>"
                + "            <code>" + encryptedPassword + "</code>"
                + "            <p>This password has been automatically saved in our system.</p>"
                + "        </div>"
                + "        <p>You can now log in:</p>"
                + "        <p><a href='" + "http://localhost:4200" + "/login' class='login-btn'>Go to Login Page</a></p>"
                + "        <p>For security reasons, we recommend you to:</p>"
                + "        <ol>"
                + "            <li>Use the new encrypted password</li>"
                + "            <li>Change your password after logging in</li>"
                + "        </ol>"
                + "        <p>Best regards,<br>The Technical Team</p>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }

}

