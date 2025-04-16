package pfe.HumanIQ.HumanIQ.emailConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailServiceImpl emailService;

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String sendMail(
            @RequestParam("recipient") String recipient,
            @RequestParam("subject") String subject,
            @RequestParam("msgBody") String msgBody,
            @RequestParam("attachment") MultipartFile attachment) {

        EmailDetails details = new EmailDetails();
        details.setRecipient(recipient);
        details.setSubject(subject);
        details.setMsgBody(msgBody);

        return emailService.sendMailWithAttachment(details, attachment);
    }


    @PostMapping("/send-verification")
    public String sendVerificationEmail(@RequestParam("username") String username) {
        String token = generateVerificationToken();
        emailService.sendVerificationEmail(username, token);
        return "Verification email sent to " + username;
    }


    private String generateVerificationToken() {
        int tokenLength = 6;
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(tokenLength);
        for (int i = 0; i < tokenLength; i++) {
            int digit = random.nextInt(9) + 1;
            token.append(digit);
        }

        return token.toString();
    }

}