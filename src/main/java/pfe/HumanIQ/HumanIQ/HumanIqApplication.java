package pfe.HumanIQ.HumanIQ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@Configuration
@EnableJpaAuditing
public class HumanIqApplication {

	public static void main(String[] args) {
		SpringApplication.run(HumanIqApplication.class, args);

	}

}
