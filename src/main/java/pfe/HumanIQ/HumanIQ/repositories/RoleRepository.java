package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.Role;
import pfe.HumanIQ.HumanIQ.models.UserRole;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByName(UserRole roleName);
}
