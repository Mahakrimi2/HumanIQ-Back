package pfe.HumanIQ.HumanIQ.services.projectservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.ProjectRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserServiceImp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserServiceImp userServiceImp;
    @Autowired
    private UserRepo userRepo;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }


    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
    }


    public Project createProject(Project project, Long projectManagerId, List<Long> employeeIds) {

        User projectManager = userServiceImp.findUserById(projectManagerId);
        if (projectManager == null) {
            throw new RuntimeException("Project Manager not found");
        }

        Role pmRole = userServiceImp.createRoleIfNotExist(UserRole.ROLE_PM);
        if (pmRole == null) {
            throw new RuntimeException("Failed to create or find ROLE_PM");
        }
        Set<Role> roles = projectManager.getRoles();
        roles.add(pmRole);
        projectManager.setRoles(roles);
        userServiceImp.updateUser(projectManager, projectManager.getId());
        List<User> employeesToAssign = userServiceImp.getUsersByIds(employeeIds);


        project.setEmployees(employeesToAssign);
        project.setProjectManager(projectManager);
        return projectRepository.save(project);
    }


    public Project updateProject(Long id, Project projectDetails) {
        Project project = getProjectById(id);
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        project.setStatus(projectDetails.getStatus());
        return projectRepository.save(project);
    }


    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }

    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }


    public List<Project> getProjectsByEmployeeUsername(String username) {
        // Trouver l'utilisateur par son username
        User employee = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le username: " + username));

        // Récupérer les projets assignés à cet utilisateur
        return projectRepository.findByEmployees_Id(employee.getId());
    }
}
