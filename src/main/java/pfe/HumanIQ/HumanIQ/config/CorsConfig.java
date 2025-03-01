//package pfe.HumanIQ.HumanIQ.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("**") // Autoriser les endpoints sous /api
//                        .allowedOrigins("http://localhost:4300") // Autoriser Angular
//                        .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS") // Méthodes autorisées
//                        .allowedHeaders("*") ;// Autoriser tous les en-têtes
//                       // .allowCredentials(true); // Autoriser les cookies
//            }
//        };
//    }
//}