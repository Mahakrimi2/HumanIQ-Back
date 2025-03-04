package pfe.HumanIQ.HumanIQ.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pfe.HumanIQ.HumanIQ.models.Role;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;

@Repository
@Transactional
public interface UserRepo extends JpaRepository<User,Long>{
    User findByUsername(String username);
    // Trouver les utilisateurs qui ne sont pas responsables d'un département
    List<User> findByDepartmentIsNull();
    List<User> findByRoles(Role role);
    List<User> findAll();
}
