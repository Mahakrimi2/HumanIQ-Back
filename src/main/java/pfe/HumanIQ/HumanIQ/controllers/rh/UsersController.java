package pfe.HumanIQ.HumanIQ.controllers.rh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailDetails;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.departmentService.DepartmentService;
import pfe.HumanIQ.HumanIQ.services.serviceAuth.TokenValidationService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rh")
@CrossOrigin(origins = "http://localhost:4300")
public class UsersController {
    private final UserService userService;
    private final TokenValidationService tokenValidationService;
    private final DepartmentService departmentService;
    private  final EmailService emailService;
    private final UserRepo userRepo;

    @Autowired
    public UsersController(UserService userService, TokenValidationService tokenValidationService, DepartmentService departmentService, EmailService emailService, UserRepo userRepo) {
        this.userService = userService;
        this.tokenValidationService = tokenValidationService;
        this.departmentService = departmentService;

        this.emailService = emailService;
        this.userRepo = userRepo;
    }


    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsersemp();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
       System.err.println ("Received user creation request: {}"+ user);
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {

        User updatedUser = userService.updateUser(user,id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }


    //Profil Image
    @PostMapping("/users/{id}/uploadProfileImage")
    public ResponseEntity<String> uploadProfileImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        final String UPLOAD_DIR = "uploads/";

        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String uniqueFileName = Instant.now().toEpochMilli() + "_" + UUID.randomUUID() + fileExtension;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setProfileImagePath(uniqueFileName);
            userRepo.save(user);

            return ResponseEntity.ok("Profile image uploaded successfully");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile image");
        }
    }

    @GetMapping("/users/profileImage/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path path = Paths.get("uploads/" + filename);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user,Long iddep) {
        try {
            System.out.println("Registering new user with username: " + user.getUsername());
            Department department =departmentService.getDepartmentById(iddep).get();
            user.setDepartment(department);
            User createdUser = userService.createUser(user);
            String token = tokenValidationService.createVerificationToken(user.getUsername());
            String subject = "Your Login Details";
            String loginUrl = "http://localhost:4300/login"; // URL de la page de login
            String message = "Welcome to our HumanIQ system!\n\n"
                    + "Here are your login details:\n"
                    + "Email: " + createdUser.getUsername() + "\n"
                    + "Password: " + user.getPassword() + "\n\n"
                    + "Click the link below to log in:\n"
                    + loginUrl;

            EmailDetails details = new EmailDetails();
            details.setRecipient(createdUser.getUsername()); // Destinataire
            details.setSubject(subject); // Sujet de l'email
            details.setMsgBody(message); // Corps de l'email

            emailService.sendSimpleMail(details);


            return ResponseEntity.status(HttpStatus.CREATED).body("User created. Please check your email to activate your account.");
        } catch (Exception e) {
            System.err.println("User registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User creation failed: " + e.getMessage());
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {

            String username = authentication.getName();

            return userRepo.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new ResourceNotFoundException("No authenticated user found");
    }
    @GetMapping("/users/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/users/profile")
    public ResponseEntity<User> updateCurrentUserProfile(@RequestBody User updatedUser) {
        User currentUser = getCurrentUser();

        currentUser.setFullname(updatedUser.getFullname());
        currentUser.setAddress(updatedUser.getAddress());
        currentUser.setProfileImagePath(updatedUser.getProfileImagePath());
        currentUser.setPassword(updatedUser.getPassword()); // Assurez-vous de hacher le mot de passe

        User savedUser = userRepo.save(currentUser);
        return ResponseEntity.ok(savedUser);
    }


}