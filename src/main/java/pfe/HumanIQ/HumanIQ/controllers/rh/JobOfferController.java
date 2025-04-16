package pfe.HumanIQ.HumanIQ.controllers.rh;

import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.JobOffer;
import pfe.HumanIQ.HumanIQ.services.JobOfferService;

import java.util.List;

@RestController
@RequestMapping("/api/rh")
public class JobOfferController {
    private final JobOfferService jobOfferService;

    public JobOfferController(JobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }

    @PostMapping("/offre")
    public JobOffer createJobOffer(@RequestBody JobOffer jobOffer) {
        return jobOfferService.createJobOffer(jobOffer);
    }

    @GetMapping("/offres")
    public List<JobOffer> getAllActiveJobOffers() {
        return jobOfferService.getAllActiveJobOffers();
    }

    @GetMapping("/{id}")
    public JobOffer getJobOfferById(@PathVariable Long id) {
        return jobOfferService.getJobOfferById(id);
    }

    @PutMapping("/{id}")
    public JobOffer updateJobOffer(@PathVariable Long id, @RequestBody JobOffer jobOfferDetails) {
        return jobOfferService.updateJobOffer(id, jobOfferDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteJobOffer(@PathVariable Long id) {
        jobOfferService.deleteJobOffer(id);
    }
}
