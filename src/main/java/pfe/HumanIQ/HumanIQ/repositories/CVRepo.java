package pfe.HumanIQ.HumanIQ.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.CV;

import java.util.List;

public interface CVRepo extends JpaRepository<CV, Long> {

    List<CV> findByJobOfferId(Long jobOfferId);
}

