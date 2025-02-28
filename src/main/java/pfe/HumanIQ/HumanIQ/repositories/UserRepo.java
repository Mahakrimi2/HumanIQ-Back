package pfe.HumanIQ.HumanIQ.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.Role;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.models.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepo extends JpaRepository<User,Long>{
    Optional<User> findByUsername(String username);
    List<User> findByDepartmentIsNull();
    List<User> findByRoles(Role role);
}
