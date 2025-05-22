package pfe.HumanIQ.HumanIQ.controllers.authControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.config.AuthenticationConfig;
import pfe.HumanIQ.HumanIQ.controllers.rh.UsersController;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailServiceImpl;
import pfe.HumanIQ.HumanIQ.emailConfig.PasswordResetTokenService;
import pfe.HumanIQ.HumanIQ.models.ForgotPwd;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private AuthenticationConfig authenticationConfig;
    @Autowired
    EmailServiceImpl emailService;
    @Autowired
    private UsersController usersController;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/forgot")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                String newPassword = passwordResetTokenService.generateRandomPassword(8);

                String encryptedPassword = authenticationConfig.passwordEncoder().encode(newPassword);

                user.setPassword(encryptedPassword);
                userRepository.save(user);

                emailService.sendPasswordResetEmail(username, newPassword);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Si cet email existe, un nouveau mot de passe sécurisé a été généré"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors du traitement"));
        }
    }
}

//    @PostMapping("/validate-token")
//    public ResponseEntity<String>validateToken(@RequestParam String token){
//        Optional<ForgotPwd> resetToken=passwordResetTokenService.getToken(token);
//        if(resetToken.isEmpty() || resetToken.get().isExpired()){
//            return ResponseEntity.badRequest().body("Invalid token ou expiré");
//        }
//        return ResponseEntity.ok("token valide");
//    }
//
//    @PostMapping("/reset")
//    public ResponseEntity<Map<String, String>> resetPassword(
//            @RequestParam String token,
//            @RequestParam String newPassword) {
//
//        Optional<ForgotPwd> resetToken = passwordResetTokenService.getToken(token);
//        if (resetToken.isEmpty() || passwordResetTokenService.isTokenExpired(resetToken.get())) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Token invalide ou expiré."));
//        }
//
//        Optional<User> user = userRepository.findByUsername(resetToken.get().getUsername());
//        if (user.isEmpty()) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Utilisateur non trouvé."));
//        }
//
//        // Encoder et sauvegarder le nouveau mot de passe
//        user.get().setPassword(authenticationConfig.passwordEncoder().encode(newPassword));
//        userRepository.save(user.get());
//
//        // Supprimer le token utilisé
//        passwordResetTokenService.deleteToken(resetToken.get());
//
//        return ResponseEntity.ok(Map.of(
//                "message", "Mot de passe réinitialisé avec succès.",
//                "status", "SUCCESS"
//        ));
//    }






