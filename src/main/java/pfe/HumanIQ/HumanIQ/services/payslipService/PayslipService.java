package pfe.HumanIQ.HumanIQ.services.payslipService;

import org.springframework.stereotype.Service;

@Service
public class PayslipService {
/*
    private final PayslipRepository payslipRepository;

    public PayslipService(PayslipRepository payslipRepository) {
        this.payslipRepository = payslipRepository;
    }

    public List<Payslip> getAllPayslips() {
        return payslipRepository.findAll();
    }

    public Optional<Payslip> getPayslipById(Long id) {
        return payslipRepository.findById(id);
    }

    public List<Payslip> getPayslipsByMonth(Date month) {
        return payslipRepository.findByMonth(month);
    }

    public Payslip createPayslip(Payslip payslip) {
        // Calcul automatique du salaire net
        payslip.setNetSalary(calculateNetSalary(payslip));
        return payslipRepository.save(payslip);
    }

    public Payslip updatePayslip(Payslip payslip) {
        payslip.setNetSalary(calculateNetSalary(payslip));
        return payslipRepository.save(payslip);
    }

    public void deletePayslip(Long id) {
        payslipRepository.deleteById(id);
    }

    private Float calculateNetSalary(Payslip payslip) {
        return payslip.getBaseSalary() + payslip.getBonuses() - payslip.getDeductions();
    }
*/
}
