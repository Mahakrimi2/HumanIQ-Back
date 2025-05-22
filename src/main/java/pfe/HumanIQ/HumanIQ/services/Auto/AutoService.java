package pfe.HumanIQ.HumanIQ.services.Auto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AutoService {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PointageRepo pointageRepository;
    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private HolidayRepository holidayRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private JavaMailSender mailSender;

//    @Scheduled(cron = "0 0 8 1 * *") // Le 1er de chaque mois à 8h

    @Scheduled(cron = "0 * * * * *") // kol de9i9a

    public void sendMonthlyPayslips() {
        List<User> employees = userRepository.findAll();

        for (User user : employees) {
            try {
                // hnee ki tena7i comment yewali kol ras chehar ye3mil calcul l3adi
               // double salary = calculateSalary(user, LocalDate.now().minusMonths(1), LocalDate.now().minusDays(1));
                 // generatePayslipPDF(user, salary, LocalDate.now().minusMonths(1), LocalDate.now().minusDays(1));
                YearMonth lastMonth = YearMonth.now().minusMonths(1);
                LocalDate startDate = lastMonth.atDay(1); // 2025-04-01
                LocalDate endDate = lastMonth.atEndOfMonth(); // 2025-04-30
                double salary = calculateSalary(user, startDate,endDate);
                generatePayslipPDF(user, salary, startDate, endDate);
                String fileName = user.getId() + "_" + startDate.getMonthValue() + ".pdf";
//                String filePath = "uploads/" + fileName;
                String filePath = "uploads" + File.separator + fileName;
// Check if the file exists
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("PDF file not found at: {}"+ filePath);
                    continue; // Skip email sending if file doesn't exist
                }
                System.out.println("PDF file successfully created at: {}"+ filePath);
                // Enregistrer dans la base
                Payslip payslip = new Payslip();
                payslip.setFilename(fileName);
                payslip.setSalary(salary);
                payslip.setGeneratedDate(LocalDate.now());
                payslip.setUser(user);
                payslipRepository.save(payslip);
                System.out.println("Payslip saved for user: {}"+ user.getUsername());

                // Envoyer la fiche de paie par email
//                FileSystemResource file = new FileSystemResource(new File(filePath));
//                sendEmailWithAttachment(user.getUsername(), file);
                FileSystemResource fileResource = new FileSystemResource(file);
                sendEmailWithAttachment(user.getUsername(), fileResource);
                System.out.println("Email sent successfully to: {}"+ user.getUsername());
            } catch (Exception e) {
                System.out.println("Erreur d'envoi pour " + user.getUsername() + " : " + e.getMessage());
            }
        }
    }

    // Calcul du salaire
    private double calculateSalary(User user, LocalDate startDate, LocalDate endDate) {
        Contract contract = contractRepository.findContractByUser(user);
        double hourlyRate = contract.getSalary() / contract.getWorkingHours();

        List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(user, startDate, endDate);
        double workedMinutes = 0;
        for (Pointage pointage : pointages) {

                workedMinutes += Duration.between(pointage.getArrivalTime(), pointage.getDepartureTime()).toMinutes();
                workedMinutes -= Duration.between(pointage.getPauseStartTime(), pointage.getPauseEndTime()).toMinutes();

        }
        double workedHours = workedMinutes / 60.0;

        // Ajout des heures correspondant aux congés payés
        List<Holiday> holidays = holidayRepository.findByUserAndStartDateBetween(user, startDate, endDate);
        for (Holiday holiday : holidays) {
            if (holiday.getStatus() == HolidayStatus.ACCEPTED && holiday.getType() == HolidayType.ANNUAL || holiday.getType()==HolidayType.SICK || holiday.getType()==HolidayType.MATERNITY) {
                workedHours += holiday.getDuration() * 8; // On suppose 8h/jour de congé payé
            }
        }

        // Déduction des congés non payés
        double holidayDeduction = 0;
        for (Holiday holiday : holidays) {
            if (holiday.getStatus() == HolidayStatus.ACCEPTED && holiday.getType() == HolidayType.UNPAID) {
                holidayDeduction += holiday.getDuration() * hourlyRate * 8; // 8h/jour non payé
            }
        }

        // ⚠️ Calcul des heures normales et supplémentaires
        double regularHours = Math.min(workedHours, contract.getWorkingHours());
        double overtimeHours = Math.max(0, workedHours - contract.getWorkingHours());

        double finalSalary = (regularHours * hourlyRate) + (overtimeHours * hourlyRate * 2) - holidayDeduction;
        return Math.max(0, finalSalary); // Évite les salaires négatifs

    }



    // Générer la fiche de paie en PDF
    private void generatePayslipPDF(User user, double salary, LocalDate startDate, LocalDate endDate) throws DocumentException, IOException {
        Contract contract = contractRepository.findContractByUser(user);
        Document document = new Document();
        File uploadsDir = new File("uploads");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }
        //PdfWriter.getInstance(document, new FileOutputStream("uploads/" + user.getId() + "_" + startDate.getMonthValue() + ".pdf"));
        String filePath = "uploads/" +File.separator+ user.getId() + "_" + startDate.getMonthValue() + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font subTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 12);

        Paragraph title = new Paragraph("Fiche de Paie", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" ", normalFont)); // Espace

        document.add(new Paragraph("Nom & Prénom : " + user.getFullname(), normalFont));
        document.add(new Paragraph("Mois concerné : " + startDate.getMonth() + " " + startDate.getYear(), normalFont));
        document.add(new Paragraph("Date de génération : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
        document.add(new Paragraph("------------------------------------------------------------"));

        document.add(new Paragraph("Informations salariales :", subTitleFont));
        document.add(new Paragraph("Salaire de base : " + String.format("%.2f", contract.getSalary()) + " TND", normalFont));
        document.add(new Paragraph("Salaire net à verser : " + String.format("%.2f", salary) + " TND", normalFont));

        document.add(new Paragraph("------------------------------------------------------------"));
        document.add(new Paragraph("Présences :", subTitleFont));

        List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(user, startDate, endDate);
        if (pointages.isEmpty()) {
            document.add(new Paragraph("Aucun pointage disponible.", normalFont));
        } else {
            for (Pointage pointage : pointages) {
                document.add(new Paragraph(
                        "Date : " + pointage.getDate() +
                                " | Arrivée : " + pointage.getArrivalTime() +
                                " | Départ : " + pointage.getDepartureTime(), normalFont
                ));
            }
        }

        document.add(new Paragraph("------------------------------------------------------------"));
        document.add(new Paragraph("Congés :", subTitleFont));

        List<Holiday> holidays = holidayRepository.findByUserAndStartDateBetween(user, startDate, endDate);
        if (holidays.isEmpty()) {
            document.add(new Paragraph("Aucun congé pris ce mois.", normalFont));
        } else {
            for (Holiday holiday : holidays) {
                document.add(new Paragraph(
                        "Type : " + holiday.getType() +
                                " | Durée : " + holiday.getDuration() + " jour(s)" +
                                " | Statut : " + holiday.getStatus(), normalFont
                ));
            }
        }

        document.close();
        System.out.println("PDF generated successfully at: "+filePath);
    }
    // Envoyer l'email avec la pièce jointe
    private void sendEmailWithAttachment(String to, FileSystemResource file) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Votre fiche de paie - " + LocalDate.now().getMonth().minus(1));
        helper.setText("Bonjour, veuillez trouver ci-joint votre fiche de paie.");
        helper.addAttachment("fiche_de_paie.pdf", file);
        mailSender.send(message);
    }
}

