package pfe.HumanIQ.HumanIQ.controllers.rh;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.DTO.CV.ParsedCVDto;
import pfe.HumanIQ.HumanIQ.models.CV;
import pfe.HumanIQ.HumanIQ.models.JobOffer;
import pfe.HumanIQ.HumanIQ.repositories.CVRepo;
import pfe.HumanIQ.HumanIQ.repositories.JobOfferRepository;
import pfe.HumanIQ.HumanIQ.services.JobOffer_CV.MatchingService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Configuration
@RequestMapping("/api/rh")
public class MatchingController {

    private final RestTemplate restTemplate;
    private final CVRepo cvRepository;
    private final MatchingService matchingService;
    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Autowired
    public MatchingController(CVRepo cvRepository, MatchingService matchingService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.cvRepository = cvRepository;
        this.matchingService = matchingService;
    }

    @PostMapping("/upload-parse-cv")
    public ResponseEntity<?> uploadAndParseCV(@RequestParam("resume") MultipartFile file) {
        try {
            ParsedCVDto parsed = matchingService.parseResume(file);

            System.out.println("parsed cv :" + parsed);

            CV cv = new CV();
            cv.setFileName(parsed.getFileName());
            cv.setName(parsed.getName());
            cv.setEmail(parsed.getEmail());
            cv.setMobile(parsed.getMobile());

            List<String> skills = new ArrayList<>();
            if (parsed.getSkills() != null) {
                skills = parsed.getSkills();

            }
            System.out.println("technicalSkills : " + skills);
            ObjectMapper mapper = new ObjectMapper();
            String skillsJson = mapper.writeValueAsString(parsed.getSkills());
            cv.setSkills(skillsJson);

            CV savedCV = cvRepository.save(cv);
            return ResponseEntity.ok(savedCV);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur de lecture du fichier : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erreur lors du parsing du CV : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne : " + e.getMessage());
        }
    }

    @GetMapping("/match/{jobId}")
    public ResponseEntity<?> matchWithJobOffer(@PathVariable Long jobId) {
        Optional<JobOffer> optionalJobOffer = jobOfferRepository.findById(jobId);
        if (optionalJobOffer.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Offre d'emploi non trouvée"));
        }

        JobOffer jobOffer = optionalJobOffer.get();
        String skillsRequired = jobOffer.getSkillsRequired();

        if (skillsRequired == null || skillsRequired.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Compétences requises manquantes"));
        }

        List<String> skillsList = Arrays.stream(skillsRequired.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        String flaskUrl = "http://localhost:5001/api/match";
        Map<String, Object> requestPayload = Map.of("skills", skillsList);

        try {
            ResponseEntity<List> response = restTemplate.postForEntity(flaskUrl, requestPayload, List.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur de communication avec le service Flask"));
        }
    }

}
