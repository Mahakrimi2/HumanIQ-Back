package pfe.HumanIQ.HumanIQ.services.Auto;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.emailConfig.EmailService;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.*;


import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Slf4j
public class Auto {
    private static final Logger logger = LoggerFactory.getLogger(Auto.class);

    private final RoleRepository roleRepository;
    private final UserRepo userRepo;
    private final PointageRepo pointageRepo;
    private final HolidayRepository holidayRepository;
    private final EmailService emailService;
    private final ContractRepository contractRepository;
    ;

    public Auto(RoleRepository roleRepository, UserRepo userRepo,
                PointageRepo pointageRepo, HolidayRepository holidayRepository,
                EmailService emailService, ContractRepository contractRepository) {
        this.roleRepository = roleRepository;
        this.userRepo = userRepo;
        this.pointageRepo = pointageRepo;
        this.holidayRepository = holidayRepository;
        this.emailService = emailService;
        this.contractRepository = contractRepository;
    }

    @Scheduled(cron = "* */2 * * * ?")
    public void generateMonthlyPayslips() {
        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        List<User> users = userRepo.findByRoles(role);
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        for (User user : users) {
            try {
                // sweeye3 5edma f chehar
                int totalWorkHours = pointageRepo.getTotalWorkHoursForUserAndMonth(user.getId(), today.getMonthValue(), today.getYear());
              // sweeye3 off f chehar
                int totalOffDays = holidayRepository.getAcceptedHolidaysForUserAndMonth(user.getId(), today.getMonthValue(), today.getYear());
               // nbr holiday annuel f chahar
                int annualLeaveDays = holidayRepository.getHolidayByTypeAndUser(user.getId(), today.getMonthValue(), today.getYear());
               // salary mte3 user
                Float baseSalary = contractRepository.getSalaryByUser(user.getId());
                // bch ne7sbo salary par jour et par heure
                Float dailySalary = baseSalary / 30;
                Float hourlySalary = dailySalary / 8;
                // Calcul du nombre d'heures à payer
                int paidWorkHours = totalWorkHours + (annualLeaveDays * 8);
                Float newSalary = paidWorkHours * hourlySalary;


                System.out.println("User: " + user.getUsername());
                System.out.println("Annual Leave Days: " + annualLeaveDays);
                System.out.println("hourlysalary: " + hourlySalary);
                System.out.println("dailysalary: " + dailySalary);
                System.out.println("Total Off Days: " + totalOffDays);
                System.out.println("Total Work Hours: " + totalWorkHours);
                System.out.println("Paid Work Hours (including annual leave): " + paidWorkHours);
                System.out.println("Old Salary: " + baseSalary);
                System.out.println("New Salary: " + newSalary);

                if (totalWorkHours > 0) {
                    byte[] payslipPdf = generatePayslipPdf(user, totalWorkHours, newSalary);
                    emailService.sendfichedepaie(user.getUsername(), "Fiche de paie - " + currentMonth, payslipPdf);
                }
            } catch (Exception e) {
                System.out.println("Error processing user " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }


    private byte[] generatePayslipPdf(User user, int totalWorkHours, double salary) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            document.add(new Paragraph("Fiche de paie"));
            document.add(new Paragraph("Employé: " + user.getFullname()));
            document.add(new Paragraph("Total Heures: " + totalWorkHours));
            document.add(new Paragraph("Salaire: " + salary + "€"));
            document.add(new Paragraph("Date: " + LocalDate.now()));
            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
