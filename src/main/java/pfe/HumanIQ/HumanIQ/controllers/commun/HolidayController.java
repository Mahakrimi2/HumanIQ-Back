package pfe.HumanIQ.HumanIQ.controllers.commun;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.Holiday;
import pfe.HumanIQ.HumanIQ.models.HolidayType;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.holidayService.HolidayService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserService;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @PutMapping("/{id}/approve")
    public ResponseEntity<Holiday> approveHoliday(@PathVariable Long id, @RequestParam String approvedBy) {
        Holiday holiday = holidayService.approveHoliday(id, approvedBy);
        return ResponseEntity.ok(holiday);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Holiday> rejectHoliday(@PathVariable Long id, @RequestParam String rejectedBy) {
        Holiday holiday = holidayService.rejectHoliday(id, rejectedBy);
        return ResponseEntity.ok(holiday);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Holiday> createHolidayRequest(
            @RequestPart("holiday") @Valid Holiday holiday,
            @RequestPart(value = "certificate", required = false) MultipartFile certificate,
            Principal principal) throws IOException { // Principal permet de récupérer l'utilisateur authentifié

        // Récupérer l'utilisateur actuellement authentifié
        String username = principal.getName(); // Récupère le nom d'utilisateur (email) de l'utilisateur authentifié
        User user = userRepo.findByUsername(username); // Récupérer l'utilisateur par email

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Gérer le cas où l'utilisateur n'est pas trouvé
        }

        // Associer l'utilisateur à la demande de congé
        holiday.setUser(user); // Assurez-vous que votre entité Holiday a un champ User et un setter pour user

        // Créer la demande de congé
        Holiday createdHoliday = holidayService.createHolidayRequest(holiday, certificate);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHoliday);
    }
    @GetMapping("/types")
    public List<String> getHolidayTypes() {
        return Arrays.stream(HolidayType.values())
                .map(Enum::name)
                .toList();
    }

}
