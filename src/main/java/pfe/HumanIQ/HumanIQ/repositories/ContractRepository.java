package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.Contract;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    // Méthodes personnalisées si nécessaire
     List<Contract> findByArchived(boolean archived);
}