package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.JobOffer;

import java.util.List;

public interface JobOfferRepository  extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByIsActiveTrue();
}
