package pfe.HumanIQ.HumanIQ.controllers.rh;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.services.pdfGeneration.PDFGeneratorService;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "http://localhost:4400")
public class PDFExportController {
    @Autowired
    private PDFGeneratorService pdfGeneratorService;
    @GetMapping("/{id}/print")
    public ResponseEntity<byte[]> printContract(@PathVariable Long id) {
        byte[] pdfBytes = pdfGeneratorService.generateContractPdf(id);

        if (pdfBytes == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=contract_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
//    @GetMapping(value = "/download-pdf/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
//    public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable Long id) throws Exception {
//        byte[] pdfBytes =pdfGeneratorService.generatePdf(id);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contrat.pdf");
//
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentLength(pdfBytes.length)
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(new ByteArrayResource(pdfBytes));
//    }
}