package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.Pointage;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;

public interface PointageRepo extends JpaRepository<Pointage, Long> {
    @Query("SELECT p FROM Pointage p WHERE p.user.username = :username")
    List<Pointage> findByUsername(@Param("username") String username);

}
