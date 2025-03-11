package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payslips")
@CrossOrigin(origins = "http://localhost:4400")
public class PayslipController {
    /*
    private final PayslipService payslipService;

    public PayslipController(PayslipService payslipService) {
        this.payslipService = payslipService;
    }

    @GetMapping
    public List<Payslip> getAllPayslips() {
        return payslipService.getAllPayslips();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payslip> getPayslipById(@PathVariable Long id) {
        return payslipService.getPayslipById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/month")
    public List<Payslip> getPayslipsByMonth(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date month) {
        return payslipService.getPayslipsByMonth(month);
    }

    @PostMapping
    public ResponseEntity<Payslip> createPayslip(@RequestBody Payslip payslip) {
        Payslip createdPayslip = payslipService.createPayslip(payslip);
        return ResponseEntity.ok(createdPayslip);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payslip> updatePayslip(@PathVariable Long id, @RequestBody Payslip payslip) {
        payslip.setId(id);
        Payslip updatedPayslip = payslipService.updatePayslip(payslip);
        return ResponseEntity.ok(updatedPayslip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayslip(@PathVariable Long id) {
        payslipService.deletePayslip(id);
        return ResponseEntity.noContent().build();
    }

     */
}
