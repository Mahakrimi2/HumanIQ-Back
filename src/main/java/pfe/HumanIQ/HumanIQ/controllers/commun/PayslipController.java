package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Payslip;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.PayslipRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.payslipService.PayslipService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/payslips")
//@CrossOrigin(origins = "http://localhost:4400")
public class PayslipController {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private UserRepo userRepository;

    @GetMapping("/my-payslips")
    public ResponseEntity<List<Payslip>> getMyPayslips(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        List<Payslip> payslips = payslipRepository.findByUserUsername(username, Payslip.class);

        if (payslips.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(payslips);
    }
    // Download a specific payslip PDF
    @GetMapping("/download/{id}")

    public ResponseEntity<Resource> downloadPayslip(@PathVariable Long id, Authentication authentication) throws IOException {
        String username = authentication.getName();
        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payslip not found"));

        // Verify the payslip belongs to the requesting user
        if (!payslip.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String filePath = "Uploads" + File.separator + payslip.getFilename();
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deletePayslip(@PathVariable Long id) {
        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found with id: " + id));

        try {
            Path filePath = Paths.get("Uploads").resolve(payslip.getFilename()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
           System.out.println("Failed to delete payslip file: " + payslip.getFilename()+ e);
            throw new RuntimeException("Failed to delete payslip file");
        }
        payslipRepository.delete(payslip);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Payslip>> getAllPayslips() {
        List<Payslip> allPayslips = payslipRepository.findAll();

        if (allPayslips.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(allPayslips);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payslip>> getPayslipsByUserId(@PathVariable Long userId) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + userId));

        // Récupérer les fiches de paie pour cet utilisateur
        List<Payslip> payslips = payslipRepository.findByUserId(userId);

        if (payslips.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(payslips);
    }

    @GetMapping("/download/adminRh/{id}")
    public ResponseEntity<Resource> downloadAdminPayslip(@PathVariable Long id, Authentication authentication) throws IOException {
        System.out.println("Superadmin download attempt for payslip ID: " + id + " by user: " + authentication.getName());
        System.out.println("User roles: " + authentication.getAuthorities());

        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payslip not found"));

        String filePath = "Uploads" + File.separator + payslip.getFilename();
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}




