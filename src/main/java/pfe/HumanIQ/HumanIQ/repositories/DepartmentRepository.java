package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pfe.HumanIQ.HumanIQ.models.*;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(DepartmentName departmentName);


}
