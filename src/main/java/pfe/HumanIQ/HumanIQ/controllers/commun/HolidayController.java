package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.Holiday;
import pfe.HumanIQ.HumanIQ.models.HolidayStatus;
import pfe.HumanIQ.HumanIQ.models.HolidayType;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.holidayService.HolidayService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/holiday")
public class HolidayController {
    @Autowired
    private HolidayService holidayService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;

    public HolidayController(HolidayService holidayService, UserService userService) {
        this.holidayService = holidayService;
        this.userService = userService;
    }

    @GetMapping
    public List<Holiday> getAllHolidays() {
        return holidayService.getAllHolidays();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Holiday> getHolidayById(@PathVariable Long id) {
        Optional<Holiday> holiday = holidayService.getHolidayById(id);
        return holiday.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @PutMapping("/{id}/approve")
//    public ResponseEntity<Holiday> approveHoliday(@PathVariable Long id, @RequestParam String approvedBy) {
//        Holiday holiday = holidayService.approveHoliday(id, approvedBy);
//        return ResponseEntity.ok(holiday);
//    }
//
//    @PutMapping("/{id}/reject")
//    public ResponseEntity<Holiday> rejectHoliday(@PathVariable Long id, @RequestParam String rejectedBy) {
//        Holiday holiday = holidayService.rejectHoliday(id, rejectedBy);
//        return ResponseEntity.ok(holiday);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<String> createHolidayRequest(
            @ModelAttribute  Holiday holiday,
            @RequestParam(value = "file",required = false) MultipartFile file,@RequestParam String email) throws IOException { // Principal permet de récupérer l'utilisateur authentifié
        final String UPLOAD_DIR = "uploads/";

        try {
            System.out.println(email);
            User user = userRepo.findByUsername(email)
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
            java.nio.file.Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            java.nio.file.Path filePath = uploadPath.resolve(uniqueFileName);

//            if (user.getProfileImagePath() != null) {
//                Path oldFilePath = uploadPath.resolve(user.getProfileImagePath());
//                try {
//                    Files.deleteIfExists(oldFilePath);
//                } catch (IOException e) {
//                     e.getMessage();
//                }
//            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


            holiday.setUser(user);
            holiday.setStatus(HolidayStatus.PENDING);
            holiday.setFicher(uniqueFileName);
            Holiday createdHoliday = holidayService.createHolidayRequest(holiday);
            return ResponseEntity.status(HttpStatus.CREATED).body("created");
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/types")
    public List<String> getHolidayTypes() {
        return Arrays.stream(HolidayType.values())
                .map(Enum::name)
                .toList();
    }
    @GetMapping("/statuses")
    public List<String> getHolidayStatus() {
        return Arrays.stream(HolidayStatus.values())
                .map(Enum::name)
                .toList();
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<Holiday> updateHolidayStatus(
            @PathVariable Long id,
            @RequestParam HolidayStatus status) {

        Holiday updatedHoliday = holidayService.updateHolidayStatus(id, status);
             return ResponseEntity.ok(updatedHoliday);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<Holiday>> getHolidaysByUsername(@PathVariable String username) {
        List<Holiday> holidays = holidayService.getHolidaysByEmpUsername(username);
        return ResponseEntity.ok(holidays);
    }
}
