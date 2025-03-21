package pfe.HumanIQ.HumanIQ.controllers.commun;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.DTO.request.ChangePasswordRequest;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailDetails;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
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
@RequestMapping("/api/users")
@Controller
public class UserController {
    private final UserService userService;
    private final TokenValidationService tokenValidationService;
    private final DepartmentService departmentService;
    private  final EmailService emailService;
    private final UserRepo userRepo;

    @Autowired
    public UserController(UserService userService, TokenValidationService tokenValidationService, DepartmentService departmentService, EmailService emailService, UserRepo userRepo) {
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
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAllUsers();
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


//    @PutMapping("/activate/{id}")
//    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
//        userService.activateUser(id);
//        return ResponseEntity.ok().build();
//    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getbyemail/{email}")
    public ResponseEntity<User> getUserById(@PathVariable String email) {
        User user = userService.findByUsername(email).get();
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
            if (originalFilename == null || !originalFilename.contains(".")) {
                return ResponseEntity.badRequest().body("Invalid file format");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String uniqueFileName = Instant.now().toEpochMilli() + "_" + UUID.randomUUID() + fileExtension;
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFileName);
            if (user.getProfileImagePath() != null) {
                Path oldFilePath = uploadPath.resolve(user.getProfileImagePath());
                try {
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete old profile image");
                }
            }
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            user.setProfileImagePath(uniqueFileName);
            userRepo.save(user);

            return ResponseEntity.ok("Profile image uploaded successfully");

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile image");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
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
    public ResponseEntity<?> register(@RequestBody User user,@RequestParam Long id) {
        try {
            System.out.println("Registering new user with username: " + user.getUsername());



            User createdUser = userService.createEmployee(user,id);
            String token = tokenValidationService.createVerificationToken(user.getUsername());
            String subject = "Your Login Details";
            String loginUrl = "http://localhost:4300/login";
            String message = "Welcome to our HumanIQ system!\n\n"
                    + "Here are your login details:\n"
                    + "Email: " + createdUser.getUsername() + "\n"
                    + "Password: " + user.getPassword() + "\n\n"
                    + "For security reasons, we recommend changing your password on your first login.\n"
                    + "You can change your password after logging in by navigating to the 'Profile' section.\n\n"
                    + "Click the link below to log in:\n"
                    + loginUrl;

            EmailDetails details = new EmailDetails();
            details.setRecipient(createdUser.getUsername());
            details.setSubject(subject);
            details.setMsgBody(message);

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
            System.out.println(username);
            return userRepo.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        throw new ResourceNotFoundException("No authenticated user found");
    }
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/users/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/profile")
    public ResponseEntity<User> updateCurrentUserProfile(@RequestBody User updatedUser) {
        User currentUser = getCurrentUser();
        currentUser.setFullname(updatedUser.getFullname());
        currentUser.setAddress(updatedUser.getAddress());
        currentUser.setPosition(updatedUser.getPosition());
        currentUser.setTelNumber(updatedUser.getTelNumber());
        currentUser.setPassword(currentUser.getPassword());
        currentUser.setProfileImagePath(updatedUser.getProfileImagePath());
        User savedUser = userRepo.save(currentUser);
        return ResponseEntity.ok(savedUser);
    }
    @DeleteMapping("/users/{id}/deleteProfileImage")
    public ResponseEntity<String> deleteProfileImage(@PathVariable Long id) {
        final String UPLOAD_DIR = "uploads/";

        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (user.getProfileImagePath() == null || user.getProfileImagePath().isEmpty()) {
                return ResponseEntity.badRequest().body("No profile image found for this user");
            }
            Path imagePath = Paths.get(UPLOAD_DIR).resolve(user.getProfileImagePath());
            Files.deleteIfExists(imagePath);
            user.setProfileImagePath(null);
            userRepo.save(user);

            return ResponseEntity.ok("Profile image deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete profile image");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }


}
