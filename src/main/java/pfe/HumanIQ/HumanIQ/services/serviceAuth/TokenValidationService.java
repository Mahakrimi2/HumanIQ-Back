package pfe.HumanIQ.HumanIQ.services.serviceAuth;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
import pfe.HumanIQ.HumanIQ.models.Token;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.TokenRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
public class TokenValidationService {

    private String activationUrl;
        private final JwtService jwtService;
        private final UserRepo userRepo;
        private final TokenRepo tokenRepo;
        private final EmailService emailService;

        public TokenValidationService(JwtService jwtService, UserRepo userRepo, TokenRepo tokenRepo, EmailService emailService) {
            this.jwtService = jwtService;
            this.userRepo = userRepo;
            this.tokenRepo = tokenRepo;
            this.emailService = emailService;
        }

    public String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    public String createVerificationToken(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        String tokenValue = generateActivationCode(6);
        Token token = new Token();
        token.setToken(tokenValue);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setUser(user);
        tokenRepo.save(token);
        System.out.println("Token enregistré : " + token.getToken());
        sendValidationEmail(user, tokenValue);
        return tokenValue;
    }
    @Transactional
    public void activateAccount(String tokenValue) {
        Token token = tokenRepo.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou inexistant."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré. Demandez un nouveau token.");
        }

        User user = userRepo.findById(token.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        user.setAccountVerified(true);
        userRepo.save(user);
        token.setValidatedAt(LocalDateTime.now());
        tokenRepo.save(token);
        tokenRepo.delete(token);
        System.out.println("Compte activé pour l'utilisateur : " + user.getUsername());
    }

    private void sendValidationEmail(User user, String token) {
        emailService.sendVerificationEmail(user.getUsername(), token);
    }
}
