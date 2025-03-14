package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {

     List<Contract> findByArchived(boolean archived);

    @Query("SELECT c FROM Contract c WHERE c.user.username = :username")
    List<Contract> findByUsername(@Param("username") String username);
    @Query("SELECT c.salary FROM Contract c WHERE c.user.id = :userId")
    Float getSalaryByUser(@Param("userId") Long userId);

}
