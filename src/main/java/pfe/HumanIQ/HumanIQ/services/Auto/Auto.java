package pfe.HumanIQ.HumanIQ.services.Auto;

import com.itextpdf.kernel.colors.DeviceRgb;
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
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private final PayslipRepository payslipRepository;

    public Auto(RoleRepository roleRepository, UserRepo userRepo,
                PointageRepo pointageRepo, HolidayRepository holidayRepository,
                EmailService emailService, ContractRepository contractRepository, PayslipRepository payslipRepository) {
        this.roleRepository = roleRepository;
        this.userRepo = userRepo;
        this.pointageRepo = pointageRepo;
        this.holidayRepository = holidayRepository;
        this.emailService = emailService;
        this.contractRepository = contractRepository;
        this.payslipRepository = payslipRepository;
    }

    @Scheduled(cron = "* * * * 1 ?")
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


                // Calculer les déductions
                double deductions = calculateDeductions(baseSalary);
                // bch ne7sbo salary par jour et par heure
                Float dailySalary = baseSalary / 22;
                Float hourlySalary = 0.0F;

                if (totalWorkHours <= 160) {
                    hourlySalary = dailySalary / 8;

                } else {
                    hourlySalary = 10.0F;
                }
                // Calcul du nombre d'heures à payer
                int paidWorkHours = totalWorkHours + (annualLeaveDays * 8);
                double newSalary = paidWorkHours * hourlySalary;

                newSalary -= deductions;

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
                    Payslip payslip = new Payslip();
                    payslip.setUser(user);
                    payslip.setBaseSalary(baseSalary);

                    payslip.setNetSalary(newSalary);

                    byte[] payslipPdf = generatePayslipPdf(user, totalWorkHours, newSalary, annualLeaveDays, baseSalary, deductions);
                    boolean emailSent = emailService.sendfichedepaie(user.getUsername(), "Fiche de paie - " + currentMonth, payslipPdf);

                    if (!emailSent) {
                        payslip.setStatus(false);
                        logger.error("Échec de l'envoi de la fiche de paie à l'employé {}", user.getUsername());
                    }
                    payslip.setStatus(true);
                    emailService.sendfichedepaie(user.getUsername(), "Fiche de paie - " + currentMonth, payslipPdf);
                    payslipRepository.save(payslip);
                }
            } catch (Exception e) {
                System.out.println("Error processing user " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }


    //    private byte[] generatePayslipPdf(User user, int totalWorkHours, double salary) {
//        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            PdfWriter writer = new PdfWriter(outputStream);
//            PdfDocument pdfDocument = new PdfDocument(writer);
//            Document document = new Document(pdfDocument);
//            document.add(new Paragraph("Fiche de paie"));
//            document.add(new Paragraph("Employé: " + user.getFullname()));
//            document.add(new Paragraph("Total Heures: " + totalWorkHours));
//            document.add(new Paragraph("Salaire: " + salary + "€"));
//            document.add(new Paragraph("Date: " + LocalDate.now()));
//            document.close();
//            return outputStream.toByteArray();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
    private double calculateDeductions(double baseSalary) {
        // Exemple de calcul des déductions (à adapter selon vos règles)
        double taxRate = 0.20; // 20% d'impôt
        double socialSecurity = 0.10; // 10% de sécurité sociale
        return (baseSalary * taxRate) + (baseSalary * socialSecurity);
    }


    LocalDate today = LocalDate.now();

    // Formater le mois et l'année (exemple : "FEBRUARY 2025")
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    String monthYear = today.format(formatter).toUpperCase();







    private byte[] generatePayslipPdf(User user, int totalWorkHours, double salary, int annualLeaveDaysUsed, double baseSalary, double deductions) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Couleurs
            com.itextpdf.kernel.colors.Color blueDark = new DeviceRgb(0, 51, 102); // Bleu foncé
            com.itextpdf.kernel.colors.Color blueLight = new DeviceRgb(173, 216, 230); // Bleu clair
            com.itextpdf.kernel.colors.Color white = ColorConstants.WHITE;

            // Titre du document
            document.add(new Paragraph("STE Resco")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(blueDark)
                    .setMarginBottom(10));

            document.add(new Paragraph("Mon Plaisir,Tunis")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(blueDark)
                    .setMarginBottom(20));

            document.add(new Paragraph("PAYSLIP")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(blueDark)
                    .setMarginBottom(20));

            document.add(new Paragraph("Month: " + monthYear)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.BLACK)
                    .setMarginBottom(20));

            // Tableau des informations de l'employé
            Table employeeInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // En-têtes du tableau des informations de l'employé
            employeeInfoTable.addHeaderCell(new Cell().add(new Paragraph("M.L.").setBold().setFontColor(white)).setBackgroundColor(blueDark));
            employeeInfoTable.addHeaderCell(new Cell().add(new Paragraph("FullName").setBold().setFontColor(white)).setBackgroundColor(blueDark));
            employeeInfoTable.addHeaderCell(new Cell().add(new Paragraph("Position").setBold().setFontColor(white)).setBackgroundColor(blueDark));

            // Données de l'employé
            employeeInfoTable.addCell(new Cell().add(new Paragraph(String.valueOf(user.getId())).setFontColor(ColorConstants.BLACK)).setBackgroundColor(blueLight));
            employeeInfoTable.addCell(new Cell().add(new Paragraph(user.getFullname()).setFontColor(ColorConstants.BLACK)).setBackgroundColor(blueLight));
            employeeInfoTable.addCell(new Cell().add(new Paragraph(user.getPosition()).setFontColor(ColorConstants.BLACK)).setBackgroundColor(blueLight));

            document.add(employeeInfoTable);

            // Tableau des détails du salaire
            Table salaryDetailsTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1})) // 3 colonnes : Description, Amount, Deductions
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            // En-têtes du tableau des détails du salaire
            salaryDetailsTable.addHeaderCell(new Cell().add(new Paragraph("Description").setBold().setFontColor(white)).setBackgroundColor(blueDark));
            salaryDetailsTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold().setFontColor(white)).setBackgroundColor(blueDark));
            salaryDetailsTable.addHeaderCell(new Cell().add(new Paragraph("Deductions").setBold().setFontColor(white)).setBackgroundColor(blueDark));

            // Alternance des couleurs des lignes
            boolean isBlue = true;
            addRow(salaryDetailsTable, "Basic Salary", String.format("%.2f", baseSalary), "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "Paid Leave", String.format("%.2f", annualLeaveDaysUsed * (baseSalary / 22)), "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "Transport Allowance", "47,000", "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "Various Bonuses", "150,000", "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "Gross Salary", String.format("%.2f", baseSalary + (annualLeaveDaysUsed * (baseSalary / 22)) + 47000 + 150000), "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "CNRS Contribution", String.format("%.2f", deductions), "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "Taxable Salary", String.format("%.2f", baseSalary + (annualLeaveDaysUsed * (baseSalary / 22)) + 47000 + 150000 - deductions), "", isBlue ? blueLight : white);
            isBlue = !isBlue;
            addRow(salaryDetailsTable, "Net Salary", String.format("%.2f", salary), "", isBlue ? blueLight : white);

            document.add(salaryDetailsTable);

            // Signature et date
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Signature: ________________________")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.BLACK));
            document.add(new Paragraph("Date: " + LocalDate.now())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.BLACK));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating PDF for employee {}", user.getUsername(), e);
            return null;
        }
    }

    // Méthode utilitaire pour ajouter une ligne au tableau avec une couleur de fond
    private void addRow(Table table, String description, String amount, String deductions, com.itextpdf.kernel.colors.Color backgroundColor) {
        table.addCell(new Cell().add(new Paragraph(description).setFontColor(ColorConstants.BLACK)).setBackgroundColor(backgroundColor));
        table.addCell(new Cell().add(new Paragraph(amount).setFontColor(ColorConstants.BLACK)).setBackgroundColor(backgroundColor));
        table.addCell(new Cell().add(new Paragraph(deductions).setFontColor(ColorConstants.BLACK)).setBackgroundColor(backgroundColor));
    }
}

