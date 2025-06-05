package pfe.HumanIQ.HumanIQ.services.JobOffer_CV;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pfe.HumanIQ.HumanIQ.models.CV;
import pfe.HumanIQ.HumanIQ.repositories.CVRepo;
import pfe.HumanIQ.HumanIQ.repositories.JobOfferRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
            // Vérifie si le fichier est vide
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide");
            }

            // Nom original du fichier
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("Nom de fichier invalide");
            }

            // Créer le dossier s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Génère un nom unique
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String uniqueFileName = "cv_" + System.currentTimeMillis() + extension;

            // Sauvegarde le fichier
            Files.copy(file.getInputStream(), uploadPath.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);

            // Crée et retourne l'objet CV
            CV cv = new CV();
            cv.setFileName(uniqueFileName);

            return cvRepository.save(cv);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier", e);
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

    public List<CV> loadCVs() {
        return cvRepository.findAll();
    }

    public void deleteCV(Long cvId) {
        cvRepository.deleteById(cvId);
    }


}


