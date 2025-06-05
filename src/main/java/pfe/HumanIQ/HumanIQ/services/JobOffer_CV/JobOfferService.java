package pfe.HumanIQ.HumanIQ.services.JobOffer_CV;

import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.JobOffer;
import pfe.HumanIQ.HumanIQ.repositories.CVRepo;
import pfe.HumanIQ.HumanIQ.repositories.JobOfferRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobOfferService {
    private static final String UPLOAD_DIR = "uploads/cvs/"; // Chemin fixe
    private final JobOfferRepository jobOfferRepository;
    private final CVRepo cvRepository;

    public JobOfferService(JobOfferRepository jobOfferRepository, CVRepo cvRepository) {
        this.jobOfferRepository = jobOfferRepository;
        this.cvRepository = cvRepository;
    }

    public JobOffer createJobOffer(JobOffer jobOffer) {
        jobOffer.setPublishedDate(LocalDate.now());
        jobOffer.setActive(true);
        return jobOfferRepository.save(jobOffer);
    }
    public List<JobOffer> getAllActiveJobOffers() {
        return jobOfferRepository.findByIsActiveTrue();
    }
    public JobOffer getJobOfferById(Long id) {
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobOffer not found with id: " + id));
    }
    public JobOffer updateJobOffer(Long id, JobOffer jobOfferDetails) {
        JobOffer existingJobOffer = getJobOfferById(id);
        existingJobOffer.setTitle(jobOfferDetails.getTitle());
        existingJobOffer.setDescription(jobOfferDetails.getDescription());
        existingJobOffer.setLocation(jobOfferDetails.getLocation());
        existingJobOffer.setContractType(jobOfferDetails.getContractType());
        existingJobOffer.setExperienceLevel(jobOfferDetails.getExperienceLevel());
        existingJobOffer.setRequiredEducation(jobOfferDetails.getRequiredEducation());
        existingJobOffer.setPublishedDate(jobOfferDetails.getPublishedDate());
        existingJobOffer.setExpirationDate(jobOfferDetails.getExpirationDate());
        existingJobOffer.setActive(jobOfferDetails.isActive());
        existingJobOffer.setSkillsRequired(jobOfferDetails.getSkillsRequired());
        existingJobOffer.setResponsibilities(jobOfferDetails.getResponsibilities());
        existingJobOffer.setBenefits(jobOfferDetails.getBenefits());
        return jobOfferRepository.save(existingJobOffer);
    }



    public void deleteJobOffer(Long id) {
        jobOfferRepository.deleteById(id);
    }
}
