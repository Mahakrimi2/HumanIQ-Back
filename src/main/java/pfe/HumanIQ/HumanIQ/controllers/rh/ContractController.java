package pfe.HumanIQ.HumanIQ.controllers.rh;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.services.contractService.ContractService;

import java.util.List;
@RestController
@RequestMapping("/api/rh")
@CrossOrigin(origins = "http://localhost:4300")
public class ContractController {
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping("/contract/{id}")
    public ResponseEntity<Contract> createContract(@RequestBody Contract contract, @PathVariable("id") Long id) {
        Contract createdContract = contractService.createContract(contract, id);
        return ResponseEntity.ok(createdContract);
    }

    @GetMapping("/contract/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable Long id) {
        return contractService.getContractById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/contracts")
    public List<Contract> getAllContracts() {
        return contractService.getAllContracts();
    }
    @GetMapping("/contractsbystatus")
    public List<Contract> getAllContractsbystat() {
        return contractService.getAllContractsbystatus();
    }

    @PutMapping("/contract/{id}")
    public ResponseEntity<Contract> updateContract(@PathVariable Long id, @RequestBody Contract updatedContract) {
        Contract updated = contractService.updateContract(id, updatedContract);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/contract/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/contract/{id}/archive")
    public ResponseEntity<Void> archiveContrat(@PathVariable Long id) {
        contractService.archiveContrat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contract/archived")
    public ResponseEntity<List<Contract>> getArchivedContrats() {
        List<Contract> archivedContrats = contractService
                .getArchivedContrats();
        return ResponseEntity.ok(archivedContrats);
    }
    @PutMapping("/contract/{id}/restore")
    public ResponseEntity<Void> restoreContrat(@PathVariable Long id) {
        contractService.restoreContrat(id);
        return ResponseEntity.ok().build();
    }


}
