package pfe.HumanIQ.HumanIQ.services.contractService;

import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.ContractRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.List;
import java.util.Optional;

@Service

public class ContractService {


    private final ContractRepository contractRepository;
    private final UserRepo userRepo;

    public ContractService(ContractRepository contractRepository, UserRepo userRepo) {
        this.contractRepository = contractRepository;
        this.userRepo = userRepo;
    }

    public List<Contract> getAllContracts() {
            return contractRepository.findAll();
    }
    public List<Contract> getAllContractsbystatus() {
        return contractRepository.findByArchived(false);
    }


    public Optional<Contract> getContractById(Long id) {
            return contractRepository.findById(id);
        }

    public Contract createContract(Contract contract, Long userId) {
        Optional<User> employeeOptional = userRepo.findById(userId);
        if (employeeOptional.isEmpty()) {
            throw new RuntimeException("Employee not found with id: " + userId);
        }
        User employee = employeeOptional.get();
        contract.setEmployee(employee);
        return contractRepository.save(contract);
    }

    public Contract updateContract(Long id, Contract updatedContract) {
        Optional<Contract> existingContractOptional = contractRepository.findById(id);
        if (existingContractOptional.isEmpty()) {
            throw new RuntimeException("Contract not found with id: " + updatedContract.getId());
        }
        Contract existingContract = existingContractOptional.get();
        existingContract.setContractType(updatedContract.getContractType());
        existingContract.setDescription(updatedContract.getDescription());
        existingContract.setStartDate(updatedContract.getStartDate());
        existingContract.setEndDate(updatedContract.getEndDate());
        existingContract.setWorkingHours(updatedContract.getWorkingHours());
        existingContract.setBenefits(updatedContract.getBenefits());
        existingContract.setSalary(updatedContract.getSalary());
        existingContract.setSigned(updatedContract.getSigned());
        existingContract.setStatus(updatedContract.getStatus());
        return contractRepository.save(existingContract);
    }

    public void deleteContract(Long id) {
            contractRepository.deleteById(id);
    }

    public void archiveContrat(Long id) {
        Contract contrat = contractRepository.findById(id).orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        contrat.setArchived(true);
        contractRepository.save(contrat);
    }

    public List<Contract> getArchivedContrats() {
        return contractRepository.findByArchived(true);
    }

    public void restoreContrat(Long id) {
        Contract contrat = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        contrat.setArchived(false);
        contractRepository.save(contrat);
    }

    public List<Contract> getContractsByUsername(String username) {
        return contractRepository.findByUsername(username);
    }

}

