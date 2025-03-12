package pfe.HumanIQ.HumanIQ.controllers.employee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.services.contractService.ContractService;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
//@CrossOrigin(origins = "http://localhost:4400")
public class EmployeeController {
    private final ContractService contractService;

    public EmployeeController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/{username}/contracts")
    public ResponseEntity<List<Contract>> getContractsByUsername(@PathVariable String username) {
        List<Contract> contracts = contractService.getContractsByUsername(username);
        return ResponseEntity.ok(contracts);
    }
}
