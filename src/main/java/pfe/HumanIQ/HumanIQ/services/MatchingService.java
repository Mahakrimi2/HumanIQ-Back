package pfe.HumanIQ.HumanIQ.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.DTO.CV.ParsedCVDto;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MatchingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ParsedCVDto parseResume(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resume", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String flaskApiUrl = "http://127.0.0.1:5000/api/upload_resume";

        ResponseEntity<String> response = restTemplate.exchange(
                flaskApiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Erreur de parsing du CV depuis l’API Flask.");
        }

        return objectMapper.readValue(response.getBody(), ParsedCVDto.class);
    }

    public List<Map<String, Object>> matchFreelancers(String skillsRequired) {
        String flaskUrl = "http://localhost:5001/api/match";

        List<String> skillList = Arrays.stream(skillsRequired.split(","))
                .map(skill -> skill.trim().toLowerCase()) // <--- FIX ici
                .collect(Collectors.toList());

        System.out.println("Skills envoyées au modèle : " + skillList);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("skills", skillList);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            System.out.println("✅ JSON envoyé à Flask : " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<List> response = restTemplate.postForEntity(flaskUrl, request, List.class);

        return response.getBody();
    }
}
