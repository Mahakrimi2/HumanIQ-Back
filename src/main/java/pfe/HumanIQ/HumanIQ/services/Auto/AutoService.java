package pfe.HumanIQ.HumanIQ.services.Auto;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
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

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
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

//    @Scheduled(cron = "0 * * * * *") // kol de9i9a

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
        if (contract == null) {
            System.out.println("No contract found for user: {}, skipping PDF generation"+ user.getUsername());
            throw new IOException("No contract found for user: " + user.getUsername());
        }

        Document document = new Document(PageSize.A4);
        File uploadsDir = new File("Uploads");
        if (!uploadsDir.exists()) {
            if (!uploadsDir.mkdirs()) {
                System.out.println("Failed to create directory: {}"+ uploadsDir.getAbsolutePath());
                throw new IOException("Unable to create uploads directory");
            }
            System.out.println("Created uploads directory: {}"+ uploadsDir.getAbsolutePath());
        }

        String fileName = user.getId() + "_" + startDate.getMonthValue() + ".pdf";
        String filePath = "Uploads" + File.separator + fileName;
        File pdfFile = new File(filePath);

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Define colors using java.awt.Color
            Color primaryColor = new Color(0, 102, 204); // Blue
            Color lightGray = new Color(240, 240, 240); // Light gray
            Color redColor = new Color(255, 0, 0); // Red

            // Define fonts
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, primaryColor);
            Font subTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD, primaryColor);
            Font normalFont = new Font(Font.HELVETICA, 12);
            Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font netSalaryFont = new Font(Font.HELVETICA, 12, Font.BOLD, redColor);

            // Header
            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.add(new Chunk("HUMAN IQ\n", titleFont));
            header.add(new Chunk("Fiche de Paie\n\n", titleFont));
            document.add(header);

            // Employee information table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            infoTable.setSpacingAfter(10f);

            PdfPCell infoHeader = new PdfPCell(new Phrase("Informations Employé", subTitleFont));
            infoHeader.setBackgroundColor(primaryColor);
            infoHeader.setColspan(2);
            infoHeader.setPadding(8f);
            infoTable.addCell(infoHeader);

            addTableRow(infoTable, "Nom & Prénom :", user.getFullname(), boldFont, normalFont);
            addTableRow(infoTable, "Mois concerné :",
                    startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + startDate.getYear(),
                    boldFont, normalFont);
            addTableRow(infoTable, "Date de génération :",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    boldFont, normalFont);
            addTableRow(infoTable, "Poste :", contract.getEmployee().getPosition(), boldFont, normalFont);

            document.add(infoTable);

            // Salary information table
            PdfPTable salaryTable = new PdfPTable(2);
            salaryTable.setWidthPercentage(100);
            salaryTable.setSpacingBefore(10f);
            salaryTable.setSpacingAfter(10f);

            PdfPCell salaryHeader = new PdfPCell(new Phrase("Informations Salariales", subTitleFont));
            salaryHeader.setBackgroundColor(primaryColor);
            salaryHeader.setColspan(2);
            salaryHeader.setPadding(8f);
            salaryTable.addCell(salaryHeader);

            addTableRow(salaryTable, "Salaire de base :", String.format("%.2f TND", contract.getSalary()), boldFont, normalFont);
            addTableRow(salaryTable, "Salaire net à verser :", String.format("%.2f TND", salary), boldFont, netSalaryFont);

            document.add(salaryTable);

            // Attendance table
            PdfPTable attendanceTable = new PdfPTable(3);
            attendanceTable.setWidthPercentage(100);
            attendanceTable.setSpacingBefore(10f);
            document.add(new Paragraph("Présences :", subTitleFont));

            PdfPCell attendanceHeader = new PdfPCell(new Phrase("Date | Arrivée | Départ", subTitleFont));
            attendanceHeader.setBackgroundColor(lightGray);
            attendanceHeader.setColspan(3);
            attendanceHeader.setPadding(8f);
            attendanceTable.addCell(attendanceHeader);

            List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(user, startDate, endDate);
            if (pointages.isEmpty()) {
                PdfPCell cell = new PdfPCell(new Phrase("Aucun pointage disponible.", normalFont));
                cell.setColspan(3);
                cell.setPadding(5f);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(lightGray);
                attendanceTable.addCell(cell);
            } else {
                for (Pointage pointage : pointages) {
                    attendanceTable.addCell(createCell(pointage.getDate().toString(), normalFont));
                    attendanceTable.addCell(createCell(pointage.getArrivalTime().toString(), normalFont));
                    attendanceTable.addCell(createCell(pointage.getDepartureTime().toString(), normalFont));
                }
            }
            document.add(attendanceTable);

            // Holidays table
            PdfPTable holidaysTable = new PdfPTable(3);
            holidaysTable.setWidthPercentage(100);
            holidaysTable.setSpacingBefore(10f);
            document.add(new Paragraph("Congés :", subTitleFont));

            PdfPCell holidaysHeader = new PdfPCell(new Phrase("Type | Durée (jours) | Statut", subTitleFont));
            holidaysHeader.setBackgroundColor(lightGray);
            holidaysHeader.setColspan(3);
            holidaysHeader.setPadding(8f);
            holidaysTable.addCell(holidaysHeader);

            List<Holiday> holidays = holidayRepository.findByUserAndStartDateBetween(user, startDate, endDate);
            if (holidays.isEmpty()) {
                PdfPCell cell = new PdfPCell(new Phrase("Aucun congé pris ce mois.", normalFont));
                cell.setColspan(3);
                cell.setPadding(5f);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(lightGray);
                holidaysTable.addCell(cell);
            } else {
                for (Holiday holiday : holidays) {
                    holidaysTable.addCell(createCell(holiday.getType().toString(), normalFont));
                    holidaysTable.addCell(createCell(String.valueOf(holiday.getDuration()) + " jour(s)", normalFont));
                    holidaysTable.addCell(createCell(holiday.getStatus().toString(), normalFont));
                }
            }
            document.add(holidaysTable);

            // Footer
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20f);
            footer.add(new Chunk("Human IQ - " + LocalDate.now().getYear() + "\n", new Font(Font.HELVETICA, 8, Font.ITALIC)));
            footer.add(new Chunk("Ce document est généré automatiquement", new Font(Font.HELVETICA, 8)));
            document.add(footer);

            document.close();
            if (pdfFile.exists() && pdfFile.canRead()) {
                System.out.println("PDF generated successfully at: {}"+ pdfFile.getAbsolutePath());
            } else {
                System.out.println("PDF file created but not accessible at: {}"+ pdfFile.getAbsolutePath());
                throw new IOException("PDF file not accessible: " + pdfFile.getAbsolutePath());
            }
        } catch (IOException | DocumentException e) {
            System.out.println("Failed to generate PDF at {}: {}"+ filePath+ e.getMessage()+ e);
            throw e;
        }
    }

    // Utility methods for PDF construction
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        table.addCell(createCell(label, labelFont));
        table.addCell(createCell(value, valueFont));
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

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