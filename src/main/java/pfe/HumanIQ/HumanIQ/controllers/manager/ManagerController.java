package pfe.HumanIQ.HumanIQ.controllers.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.services.projectservice.ProjectService;
import pfe.HumanIQ.HumanIQ.services.serviceUser.UserServiceImp;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserServiceImp userServiceImp;

    // Get all projects
    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    // Get a project by ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody Project project,
            @RequestParam Long projectManagerId,
            @RequestParam List<Long> employeeIds) {

        Project createdProject = projectService.createProject(project, projectManagerId, employeeIds);
        return ResponseEntity.ok(createdProject);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody Project projectDetails) {

        Project updatedProject = projectService.updateProject(id, projectDetails);
        return ResponseEntity.ok(updatedProject);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public List<ProjectStatus> getProjectsByStatus() {
        return Arrays.asList(ProjectStatus.values());
    }

    @GetMapping("/priority")
    public List<PriorityName> getProjectsByPriority() {
        return Arrays.asList(PriorityName.values());
    }





}
