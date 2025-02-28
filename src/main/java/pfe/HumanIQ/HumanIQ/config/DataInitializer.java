package pfe.HumanIQ.HumanIQ.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import pfe.HumanIQ.HumanIQ.models.UserRole;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserServiceImp;

@Configuration
public class DataInitializer implements CommandLineRunner {
    private final UserServiceImp userService;


    public DataInitializer(UserServiceImp userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.createRoleIfNotExist(UserRole.ROLE_RH);
        userService.createRoleIfNotExist(UserRole.ROLE_ADMIN);
        userService.createRoleIfNotExist(UserRole.ROLE_EMPLOYEE);
    }
}
