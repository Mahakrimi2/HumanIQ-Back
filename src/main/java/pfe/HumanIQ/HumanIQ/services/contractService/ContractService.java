package pfe.HumanIQ.HumanIQ.services.contractService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.ContractRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@Slf4j
public class ContractService {


    private final ContractRepository contractRepository;
    private final UserRepo userRepo;
    private final NotificationService notificationService;

    public ContractService(ContractRepository contractRepository, UserRepo userRepo, NotificationService notificationService) {
        this.contractRepository = contractRepository;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
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




    public void checkAndNotifyExpiringContracts() {
        System.out.println("\n=== Vérification des contrats à renouveler ===");
        LocalDate now = LocalDate.now();
        LocalDate thresholdDate = now.plusDays(1); // 30 jours avant expiration

        List<Contract> expiringContracts = contractRepository
                .findByEndDateBetweenAndArchivedFalse(now, thresholdDate);

        System.out.println(expiringContracts.size() + " contrats à renouveler trouvés");

        expiringContracts.forEach(contract -> {
            System.out.println("\nTraitement du contrat ID: " + contract.getId());
            System.out.println("Employé: " + contract.getUser().getFullname());
            System.out.println("Date fin: " + contract.getEndDate());

            notificationService.notifyAboutContractRenewal(contract);
        });
    }

    // Méthode appelée par le scheduler
   @Scheduled(cron = "0 0 9 * * ?") // Tous les jours à 9h
   // @Scheduled(cron = "0 * * * * ?")
    public void scheduledContractRenewalCheck() {
        System.out.println("\n=== Exécution planifiée - Vérification des contrats ===");
        checkAndNotifyExpiringContracts();
    }

}

