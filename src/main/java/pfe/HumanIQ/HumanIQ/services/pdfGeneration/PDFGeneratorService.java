package pfe.HumanIQ.HumanIQ.services.pdfGeneration;
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
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.repositories.ContractRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PDFGeneratorService {
    @Autowired
    private ContractRepository contractRepository;

    //    public byte[] generatePdf(Long contratId) throws DocumentException {
//        Optional<Contract> contratOptional = contractRepository.findById(contratId);
//        if (contratOptional.isEmpty()) {
//            throw new RuntimeException("Contrat non trouvé");
//        }
//
//        Contract contract = contratOptional.get();
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        Document document = new Document();
//        PdfWriter.getInstance(document, outputStream);
//
//        document.open();
//        document.add(new Paragraph(" Employee Fullname :"+contract.getEmployee().getFullname()));
//        document.add(new Paragraph("Company Adress : Mon Plaisir" ));
//        document.add(new Paragraph("Object : " + contract.getContractType()));
//        document.add(new Paragraph("Position : " + contract.getEmployee().getPosition()));
//        document.add(new Paragraph("Client : " + contract.getBenefits()));
//        document.add(new Paragraph("Client : " + contract.getBenefits()));
//        document.add(new Paragraph("Date de signature : " + contract.getStartDate()));
//        document.add(new Paragraph("Date de signature : " + contract.getBenefits()));
//        document.add(new Paragraph("Date de signature : " + contract.getStatus()));
//        document.add(new Paragraph("Date de signature : " + contract.getEmployee().getFullname()));
//        document.close();
//        return outputStream.toByteArray();
//    }
    public byte[] generateContractPdf(Long contractId) {
        Optional<Contract> contractOptional = contractRepository.findById(contractId);
        if (contractOptional.isEmpty()) {
            return null;
        }

        Contract contract = contractOptional.get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.setMargins(50, 50, 50, 50);
            // Header
            Table headerTable = new Table(2).useAllAvailableWidth();
            headerTable.addCell(new Cell().add(new Paragraph("Resco Development LLC").setFontSize(16).setBold()).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("Date: " + LocalDate.now())).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            document.add(headerTable);
            document.add(new LineSeparator(new SolidLine()));

            // Title
            document.add(new Paragraph("\n" + contract.getContractType().toString().toUpperCase() + " AGREEMENT")
                    .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // 1. PARTIES IDENTIFICATION
            document.add(new Paragraph("1. PARTIES IDENTIFICATION").setBold().setFontSize(14));
            Table partiesTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();

            addStyledCell(partiesTable, "Company", "Resco Development: +216 74 556 656\nAddress: Av. Kheireddine Pacha, 48, Tunis, Monplaisir");
            addStyledCell(partiesTable, "Employee", contract.getEmployee().getFullname() +
                    "\nAddress: " + contract.getEmployee().getAddress() +
                    "\nPhone: " + contract.getEmployee().getTelNumber() +
                    "\nEmail: " + contract.getEmployee().getUsername());
            document.add(partiesTable);
            document.add(new Paragraph("\n"));

            // 2. CONTRACT DETAILS
            document.add(new Paragraph("2. CONTRACT DETAILS").setBold().setFontSize(14));
            Table contractTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();

            addStyledCell(contractTable, "Contract Type", contract.getContractType().toString());
            addStyledCell(contractTable, "Start Date", contract.getStartDate().toString());
            addStyledCell(contractTable, "End Date", contract.getEndDate().toString());
            addStyledCell(contractTable, "Working Hours", contract.getWorkingHours() +
                    "\nMonday-Friday: 09:00-12:30 / 14:00-18:00");
            addStyledCell(contractTable, "Monthly Gross Salary", contract.getSalary().toString() + " TND" +
                    "\nPayment on the 5th of each month by bank transfer");

            // Gestion flexible de la description
            addFlexibleTextCell(contractTable, "Description", contract.getDescription(), document);

            // Gestion flexible des benefits
            String benefitsText = contract.getBenefits() +
                    "\n- Health insurance\n- Meal vouchers\n- 50% transportation coverage";
            addFlexibleTextCell(contractTable, "Benefits", benefitsText, document);

            document.add(contractTable);
            document.add(new Paragraph("\n"));

            // 3. CONTRACTUAL CLAUSES
            document.add(new Paragraph("3. CONTRACTUAL CLAUSES").setBold().setFontSize(14));

            // Confidentiality
            document.add(new Paragraph("3.1 Confidentiality").setBold());
            document.add(new Paragraph("The employee agrees not to disclose the company's confidential information " +
                    "during and after the term of this agreement. This obligation remains valid for 5 years after the contract ends."));

            // Intellectual Property
            document.add(new Paragraph("3.2 Intellectual Property").setBold());
            document.add(new Paragraph("All creations made under this agreement are the exclusive property of the company."));

            // Non-competition
            document.add(new Paragraph("3.3 Non-competition").setBold());
            document.add(new Paragraph("During the term of this agreement and for 12 months after its termination, the employee agrees not to work " +
                    "for a competing company within a 50km radius."));

            // Termination
            document.add(new Paragraph("3.4 Termination").setBold());
            document.add(new Paragraph("In case of early termination, a notice period of " + "contract.getNoticePeriod()" + " months must be respected. " +
                    "Any termination must be notified in writing."));

            // Renewal
            document.add(new Paragraph("3.5 Renewal").setBold());
            document.add(new Paragraph("This agreement may be renewed by written agreement of both parties at least 1 month before its expiration."));

            // Disputes
            document.add(new Paragraph("3.6 Disputes").setBold());
            document.add(new Paragraph("In case of dispute, both parties agree to seek an amicable solution. " +
                    "Failing that, the courts of [City] will have exclusive jurisdiction."));

            document.add(new Paragraph("\n"));

            // 4. APPLICABLE LAW
            document.add(new Paragraph("4. APPLICABLE LAW").setBold().setFontSize(14));
            document.add(new Paragraph("This agreement is governed by Tunisian law, and in particular by the Tunisian Labor Code."));
            document.add(new Paragraph("\n"));

            // 5. SIGNATURES
            document.add(new Paragraph("5. SIGNATURES").setBold().setFontSize(14));
            document.add(new Paragraph("Executed in two original copies at " + "company" +
                    ", on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))));

            Table signatureTable = new Table(2).useAllAvailableWidth().setMarginTop(30);
            // Employee signature
            signatureTable.addCell(new Cell()
                    .add(new Paragraph("For the employee:\n\n\n"))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER));

            // Company signature
            signatureTable.addCell(new Cell()
                    .add(new Paragraph("For the company:\n\n\n"))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(signatureTable);

            // Legal mentions
            document.add(new Paragraph("\n\nRead and approved,\nAccepted for agreement")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            // Page numbers
            int numberOfPages = pdfDocument.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                document.showTextAligned(new Paragraph(String.format("Page %d/%d", i, numberOfPages))
                                .setFontSize(8),
                        559, 20, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addStyledCell(Table table, String header, String value) {
        table.addCell(new Cell().add(new Paragraph(header)).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    private void addFlexibleTextCell(Table table, String header, String content, Document document) {
        // Cellule pour le header
        table.addCell(new Cell().add(new Paragraph(header)).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));

        // Estimation de la longueur du texte (environ 100 caractères par ligne)
        if (content != null && content.length() > 300) {
            // Si le texte est long, on crée une cellule avec un paragraphe qui s'étendra sur plusieurs pages
            Cell contentCell = new Cell();
            contentCell.setKeepTogether(true); // Essaye de garder le contenu ensemble si possible

            // Découpage en paragraphes si le texte contient des sauts de ligne
            if (content.contains("\n")) {
                String[] paragraphs = content.split("\n");
                for (String para : paragraphs) {
                    contentCell.add(new Paragraph(para).setMarginBottom(5));
                }
            } else {
                contentCell.add(new Paragraph(content));
            }

            table.addCell(contentCell);
        } else {
            // Texte court - traitement normal
            table.addCell(new Cell().add(new Paragraph(content != null ? content : "N/A")));
        }
    }


}