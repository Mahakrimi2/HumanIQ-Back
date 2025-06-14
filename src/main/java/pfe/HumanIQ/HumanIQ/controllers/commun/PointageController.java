package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Pointage;
import pfe.HumanIQ.HumanIQ.models.PointageStatus;
import pfe.HumanIQ.HumanIQ.repositories.PointageRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.Pointages.PointageService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/pointages")
//@CrossOrigin(origins = "http://localhost:4400")
public class PointageController {

    @Autowired
    private PointageService pointageService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PointageRepo pointageRepo;

    @PostMapping("/create/{username}")
    public Pointage createPointage(@PathVariable String username, @RequestBody Pointage pointage) {

        return pointageService.createPointage(username,pointage);
    }

    private Duration calculateWorkingTime(Pointage pointage) {
       if (pointage.getDepartureTime()!=null){
           Duration totalTime = Duration.between(pointage.getArrivalTime(), pointage.getDepartureTime());
           if (pointage.getPauseStartTime() != null && pointage.getPauseEndTime() != null) {
               Duration pauseDuration = Duration.between(pointage.getPauseStartTime(), pointage.getPauseEndTime());
               totalTime = totalTime.minus(pauseDuration);
           }

           return totalTime;
       }
       return null;


    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Pointage>> getPointagesByUser(@PathVariable String username) {
        List<Pointage> pointages = pointageService.getPointagesByUser(username);
        return ResponseEntity.ok(pointages);
    }
    @GetMapping("/all")

    public ResponseEntity<List<Pointage>> getAllPointages() {
        List<Pointage> pointages = pointageRepo.findAll(); // Récupérer tous les pointages
        return ResponseEntity.ok(pointages);
    }


    public ResponseEntity<List<Pointage>> getPointages() {
        List<Pointage> pointages = pointageService.getall();
        return ResponseEntity.ok(pointages);
    }
    @GetMapping("/byid/{id}")
    public Pointage getPointagesByid(@PathVariable Long id) {
        Pointage pointages = pointageService.getbyid(id);
        return pointages;
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Pointage> updatePointage(@PathVariable Long id, @RequestBody Pointage pointageDetails) {
        Pointage pointage = pointageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pointage not found with id: " + id));
        pointage.setArrivalTime(pointageDetails.getArrivalTime());
        pointage.setPauseStartTime(pointageDetails.getPauseStartTime());
        pointage.setPauseEndTime(pointageDetails.getPauseEndTime());
        pointage.setDepartureTime(pointageDetails.getDepartureTime());

        Pointage updatedPointage = pointageRepo.save(pointage);
        return ResponseEntity.ok(updatedPointage);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePointage(@PathVariable Long id) {
        Pointage pointage = pointageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pointage not found with id: " + id));

        pointageRepo.delete(pointage);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/status")
    public List<String> getHolidayTypes() {
        return Arrays.stream(PointageStatus.values())
                .map(Enum::name)
                .toList();
    }
    @GetMapping("/today-status/{username}")
    public ResponseEntity<Map<String, Object>> getTodayPointageStatus(
            @PathVariable String username) {

        LocalDate today = LocalDate.now();
        Optional<Pointage> pointageOpt = pointageRepo.findByUserUsernameAndDate(username, today);

        Map<String, Object> response = new HashMap<>();

        if (pointageOpt.isPresent()) {
            Pointage pointage = pointageOpt.get();
            response.put("hasArrival", pointage.getArrivalTime() != null);
            response.put("hasDeparture", pointage.getDepartureTime() != null);
            response.put("hasPause", pointage.getPauseStartTime() != null && pointage.getPauseEndTime() != null);
        } else {
            response.put("hasArrival", false);
            response.put("hasDeparture", false);
            response.put("hasPause", false);
        }

        return ResponseEntity.ok(response);
    }


}
