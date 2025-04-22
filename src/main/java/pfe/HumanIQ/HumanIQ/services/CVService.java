package pfe.HumanIQ.HumanIQ.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.CV;
import pfe.HumanIQ.HumanIQ.models.JobOffer;
import pfe.HumanIQ.HumanIQ.repositories.CVRepo;
import pfe.HumanIQ.HumanIQ.repositories.JobOfferRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class CVService {
    private static final String UPLOAD_DIR = "uploads/cvs/";
    private final CVRepo cvRepository;
    private final JobOfferRepository jobOfferRepository;

    public CVService(CVRepo cvRepository, JobOfferRepository jobOfferRepository) {
        this.cvRepository = cvRepository;
        this.jobOfferRepository = jobOfferRepository;
    }



    public CV uploadCV(MultipartFile file) {
        try {
//            JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée"));



            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) throw new IllegalArgumentException("Nom invalide");


            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }


            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String uniqueFileName = "cv_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;


            Files.copy(file.getInputStream(), uploadPath.resolve(uniqueFileName),
                    StandardCopyOption.REPLACE_EXISTING);


            CV cv = new CV();
            cv.setFileName(uniqueFileName);
//            cv.setJobOffer(jobOffer);

            return cvRepository.save(cv);
        } catch (IOException e) {
            throw new RuntimeException("Erreur fichier", e);
        }
    }

    public Resource downloadCV(Long cvId) {
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV introuvable"));

        Path filePath = Paths.get(UPLOAD_DIR + cv.getFileName());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("Fichier illisible");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur chemin fichier", e);
        }
    }
    public List<CV> getCVsByJobOffer() {
        return cvRepository.findAll();
    }


}
