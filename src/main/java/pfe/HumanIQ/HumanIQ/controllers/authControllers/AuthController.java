package pfe.HumanIQ.HumanIQ.controllers.authControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.DTO.response.LoginDTO;
import pfe.HumanIQ.HumanIQ.DTO.request.AuthRequest;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
import pfe.HumanIQ.HumanIQ.models.Token;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.serviceAuth.JwtService;
import pfe.HumanIQ.HumanIQ.services.serviceAuth.TokenValidationService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserService;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@Validated
@CrossOrigin(origins = "https://localhost:4400")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TokenValidationService tokenValidationService;
    private final UserRepo userRepository;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService, EmailService emailService, TokenValidationService tokenValidationService, UserRepo userRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService=emailService;
        this.tokenValidationService = tokenValidationService;
        this.userRepository = userRepository;
    }

//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody User user) {
//
//        try {
//            System.out.println("Registering new user with username: " + user.getUsername());
//            User createdUser = userService.createUser(user);
//            tokenValidationService.createVerificationToken(user.getUsername());
//
//        return ResponseEntity.status(HttpStatus.CREATED).body("User created. Please check your email to activate your account.");
//        } catch (Exception e) {
//            System.err.println("User registration failed: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User creation failed: " + e.getMessage());
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<LoginDTO> login(@RequestBody AuthRequest authRequest) {
    try {
        if (authRequest.getUsername() == null || authRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(new LoginDTO("Username and password must not be empty"));
        }

        System.out.println("Login attempt for email: " + authRequest.getUsername());
        
        User user = userService.findByUsername(authRequest.getUsername())
            .orElseThrow(() -> {
                System.err.println("User not found: " + authRequest.getUsername());
                return new BadCredentialsException("User not found");
            });
        
        System.out.println("Found user in database: " + user.getUsername());
        if (user.getIsDisabled().equals(true)) {
            System.err.println("Account is desactivated for user: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new LoginDTO("Account is deactivated. Please contact the administrator."));
        }


        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        if (authenticate.isAuthenticated()) {
            System.out.println("Authentication successful for user: " + authRequest.getUsername());
            String token = jwtService.generateToken(authRequest.getUsername());
            return ResponseEntity.ok(new LoginDTO(token));
        } else {
            System.err.println("Authentication failed for user: " + authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginDTO("Invalid credentials"));
        }
    } catch (BadCredentialsException e) {
        System.err.println("Bad credentials for user " + authRequest.getUsername() + ": " + e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginDTO("Invalid credentials"));
    } catch (Exception e) {
        System.err.println("Authentication error for user " + authRequest.getUsername() + ": " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginDTO("Authentication failed"));
    }
}
    @GetMapping("/user-info")
    public ResponseEntity<User> getUserInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

/*
    @PostMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestParam String token) {
        try {
            tokenValidationService.activateAccount(token);
            return ResponseEntity.status(HttpStatus.OK).body("Account activated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation failed: " + e.getMessage());
        }
    }

*/
    @PostMapping("/validate-account")
    public ResponseEntity<?> validateAccount(@RequestBody Token tokenRequest) {
        try {
            tokenValidationService.activateAccount(tokenRequest.getToken());
            return ResponseEntity.ok(Collections.singletonMap("message", "Compte activé avec succès !"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
