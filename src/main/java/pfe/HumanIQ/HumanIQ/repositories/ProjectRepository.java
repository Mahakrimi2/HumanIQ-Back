package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.PriorityName;
import pfe.HumanIQ.HumanIQ.models.Project;
import pfe.HumanIQ.HumanIQ.models.ProjectStatus;
import pfe.HumanIQ.HumanIQ.models.User;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByProjectStatus(ProjectStatus status);
    List<Project> findByEmployees_Id(Long employeeId);
    List<Project> findByPriority(PriorityName priorityName);
    List<Project> findByEmployeesContaining(User user);
    List<Project> findByProjectManager(User projectManager);


}
