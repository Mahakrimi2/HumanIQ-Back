package pfe.HumanIQ.HumanIQ;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.Role;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.models.UserRole;
import pfe.HumanIQ.HumanIQ.repositories.RoleRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.time.LocalDate;

@SpringBootApplication
@Configuration
@EnableJpaAuditing
@EnableScheduling
@Component
public class HumanIqApplication implements CommandLineRunner {
	private final UserRepo userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

    public HumanIqApplication(UserRepo userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
		SpringApplication.run(HumanIqApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {

		if (userRepository.findByUsername("superadmin@gmail.com").isEmpty()) {

			Role adminRole = roleRepository.findByName(UserRole.ROLE_SUPERADMIN);

			// Créer l'utilisateur admin
			User admin = new User();

			admin.setUsername("superadmin@gmail.com");
			admin.setGender("female");
			admin.setFullname("maha");
			admin.setAddress("Tunis, Tunisia");
			admin.setNationalID("123456789");
			admin.setPosition("full-stack developer");
			admin.setSalary(5000.0);
			admin.setDateOfBirth(LocalDate.of(1990, 5, 15));
			admin.setTelNumber("25 236 255");
			admin.setHireDate(LocalDate.of(2022, 6, 1));
			admin.setEmail("superadmin@gmail.com");
			admin.setPassword(passwordEncoder.encode("admin123"));
			admin.getRoles().add(adminRole);


			userRepository.save(admin);
			System.out.println("Admin user created successfully!");
		} else {
			System.out.println("Admin user already exists.");
		}
	}
	}

