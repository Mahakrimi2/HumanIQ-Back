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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.DTO.request.ChangePasswordRequest;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailDetails;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.Role;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.models.UserRole;
import pfe.HumanIQ.HumanIQ.repositories.RoleRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.departmentService.DepartmentService;
import pfe.HumanIQ.HumanIQ.services.serviceAuth.TokenValidationService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserServiceImp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rh")
@Controller
public class UsersController {
    private final UserService userService;
    private final TokenValidationService tokenValidationService;
    private final DepartmentService departmentService;
    private  final EmailService emailService;
    private final UserRepo userRepo;
    private final UserServiceImp userServiceImp;
    private final RoleRepository roleRepository;

    @Autowired
    public UsersController(UserService userService, TokenValidationService tokenValidationService, DepartmentService departmentService, EmailService emailService, UserRepo userRepo, UserServiceImp userServiceImp, RoleRepository roleRepository) {
        this.userService = userService;
        this.tokenValidationService = tokenValidationService;
        this.departmentService = departmentService;

        this.emailService = emailService;
        this.userRepo = userRepo;
        this.userServiceImp = userServiceImp;
        this.roleRepository = roleRepository;
    }


    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsersEmp() {
        List<User> users = userService.getAllUsersemp();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userServiceImp.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all-with-roles")
    public List<User> getAllUsersWithRoles() {
        return userServiceImp.getAllUsersWithRoles();
    }
    @GetMapping("/count-by-role")
    public Map<String, Long> getCountOfUsersByRole() {
        return userServiceImp.getCountOfUsersByRole();
    }
    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user,@RequestParam Long id) {
       System.err.println ("Received user creation request: {}"+ user);
        User createdUser = userService.createUser(user,id);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {

        User updatedUser = userService.updateUser(user,id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/userDisactivate/{id}")
    public ResponseEntity<Void> disactivateUser(@PathVariable Long id) {
        userServiceImp.disactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/user/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        userServiceImp.enableUser(id);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<?> register(@RequestBody User user, @RequestParam Long id) {
        try {
            System.out.println("Registering new user with username: " + user.getUsername());

            User createdUser = userService.createUser(user, id);
            String token = tokenValidationService.createVerificationToken(user.getUsername());
            String subject = "Your HumanIQ Account Details";
            String loginUrl = "https://localhost:4200/login";
            String message = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<style>"
                    + "  body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }"
                    + "  .header { background-color: #4a90e2; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }"
                    + "  .header h1 { color: white; margin: 0; }"
                    + "  .content { padding: 25px; background-color: #f9f9f9; border-radius: 0 0 8px 8px; border: 1px solid #e0e0e0; }"
                    + "  .button { display: inline-block; padding: 12px 24px; background-color: #28a745; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 15px 0; }"
                    + "  .button:hover { background-color: #218838; }"
                    + "  .credentials { background-color: #e9f7ef; padding: 15px; border-radius: 6px; margin: 20px 0; }"
                    + "  .footer { margin-top: 30px; font-size: 12px; color: #777; text-align: center; }"
                    + "  @media only screen and (max-width: 600px) {"
                    + "    .content { padding: 15px; }"
                    + "    .button { display: block; text-align: center; }"
                    + "  }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class='header'>"
                    + "<h1>Welcome to HumanIQ</h1>"
                    + "</div>"
                    + "<div class='content'>"
                    + "<p>Hello,</p>"
                    + "<p>Your account has been successfully created. Here are your login details:</p>"
                    + "<div class='credentials'>"
                    + "<p><strong>Email:</strong> " + createdUser.getUsername() + "</p>"
                    + "<p><strong>Password:</strong> " + user.getPassword() + "</p>"
                    + "</div>"
                    + "<p>For security reasons, we recommend changing your password after your first login.</p>"
                    + "<a href='" + loginUrl + "' class='button'>Login to Your Account</a>"
                    + "<p>If you have any questions, please contact our support team.</p>"
                    + "<div class='footer'>"
                    + "<p>© " + LocalDate.now().getYear() + " HumanIQ. All rights reserved.</p>"
                    + "<p>This is an automated message, please do not reply.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

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

    @GetMapping("/roles")
    public ResponseEntity<UserRole[]> getAllRoles() {
        return ResponseEntity.ok(UserRole.values());
    }
}
