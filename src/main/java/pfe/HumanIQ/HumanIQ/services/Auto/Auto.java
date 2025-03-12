package pfe.HumanIQ.HumanIQ.services.Auto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Pointage;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.PointageRepo;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.List;

@Service
public class Auto {
    @Autowired
    private PointageRepo pointageRepo;
    @Autowired
    private UserRepo userRepo;
   // @Scheduled(cron = "* */1 * * * *")
    public void auto() {
        List<Pointage> pointages = pointageRepo.findAll();
        List<User> users = userRepo.findAll();
        for (Pointage pointage : pointages) {
            for(User user : users) {
                if(pointage.getUser().getId() == user.getId()) {
                    System.out.println("id = id");
                }
            }
        }
    }
}
