package pfe.HumanIQ.HumanIQ.controllers.employee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.Project;
import pfe.HumanIQ.HumanIQ.services.contractService.ContractService;
import pfe.HumanIQ.HumanIQ.services.projectservice.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
//@CrossOrigin(origins = "http://localhost:4400")
public class EmployeeController {
    private final ContractService contractService;

    private final ProjectService projectService;

    public EmployeeController(ContractService contractService, ProjectService projectService) {
        this.contractService = contractService;
        this.projectService = projectService;
    }

    @GetMapping("/{username}/contracts")
    public ResponseEntity<List<Contract>> getContractsByUsername(@PathVariable String username) {
        List<Contract> contracts = contractService.getContractsByUsername(username);
        return ResponseEntity.ok(contracts);
    }


    @GetMapping("/{username}/projects")
    public ResponseEntity<List<Project>> getProjectsByEmployeeUsername(@PathVariable String username) {
        List<Project> projects = projectService.getProjectsByEmployeeUsername(username);
        return ResponseEntity.ok(projects);
    }
}
