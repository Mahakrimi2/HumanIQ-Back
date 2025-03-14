package pfe.HumanIQ.HumanIQ.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Pointage;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.PointageRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.time.Duration;
import java.util.List;

@Service
public class PointageService {
    @Autowired
    private PointageRepo pointageRepository;

    @Autowired
    private UserRepo userRepository;


    public Pointage createPointage(String username, Pointage pointage) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        pointage.setUser(user);
        Double workingTimeString = calculateWorkingTime(pointage);
        pointage.setWorkingTime(workingTimeString);

        return pointageRepository.save(pointage);
    }

    public List<Pointage> getPointagesByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return pointageRepository.findByUsername(username);
    }
    public List<Pointage> getall() {

        return pointageRepository.findAll();
    }

    public Pointage getbyid(Long id) {

        return pointageRepository.findById(id).get();
    }

    private Double calculateWorkingTime(Pointage pointage) {
        if (pointage.getDepartureTime() != null) {
            Duration totalTime = Duration.between(pointage.getArrivalTime(), pointage.getDepartureTime());
            if (pointage.getPauseStartTime() != null && pointage.getPauseEndTime() != null) {
                Duration pauseDuration = Duration.between(pointage.getPauseStartTime(), pointage.getPauseEndTime());
                totalTime = totalTime.minus(pauseDuration);
            }
            long hours = totalTime.toHours();
            long minutes = totalTime.toMinutes() % 60;
            return Double.valueOf(hours+minutes);
        }
        return  0.0 ;
    }
}

