package pfe.HumanIQ.HumanIQ.controllers.rh;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.CV;
import pfe.HumanIQ.HumanIQ.services.CVService;

import java.util.List;

@RestController
@RequestMapping("/api/rh")
public class CVController {
    private final CVService cvService;

    public CVController(CVService cvService) {
        this.cvService = cvService;
    }

    @PostMapping("/job-offer/{jobOfferId}")
    public ResponseEntity<CV> uploadCV(@PathVariable Long jobOfferId,
                                       @RequestParam("file") MultipartFile fileName) {
        return ResponseEntity.ok(cvService.uploadCV(jobOfferId, fileName));
    }

    @GetMapping("/{cvId}/download")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long cvId) {
        Resource resource = cvService.downloadCV(cvId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
