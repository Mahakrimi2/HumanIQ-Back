package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
