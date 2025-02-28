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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.repositories.ContractRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
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

            Table headerTable = new Table(2).useAllAvailableWidth();
            headerTable.addCell(new Cell().add(new Paragraph("HumanIQ").setFontSize(16).setBold()).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("Date : " + LocalDate.now())).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            document.add(headerTable);
            document.add(new LineSeparator(new SolidLine()));

            document.add(new Paragraph("\nCONTRAT " + contract.getContractType().toString().toUpperCase())
                    .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            table.setMarginBottom(20);

            addStyledCell(table, "ID du contrat", String.valueOf(contract.getId()));
            addStyledCell(table, "Nom du client", contract.getEmployee().getFullname());
            addStyledCell(table, "Date de début", contract.getStartDate().toString());
            addStyledCell(table, "Date de fin", contract.getEndDate().toString());
            addStyledCell(table, "Description", contract.getDescription());
            addStyledCell(table, "Benefits", contract.getBenefits());
            addStyledCell(table, "salary", contract.getSalary().toString() + "tn");
            addStyledCell(table, "Heure de travail", contract.getWorkingHours().toString());
            document.add(table);

            document.add(new Paragraph("Conditions Générales").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("1. Le client accepte les termes du contrat.\n2. Toute violation entraînera des sanctions légales.\n3. Le paiement doit être effectué à la date prévue.").setFontSize(10));

            Table signatureTable = new Table(2).useAllAvailableWidth();
            signatureTable.addCell(new Cell().add(new Paragraph("Signature du client : "))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));

            signatureTable.addCell(new Cell().add(new Paragraph("Signature de l'entreprise : "))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("\n")); // Ajout d'un espace
            document.add(signatureTable);

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
}