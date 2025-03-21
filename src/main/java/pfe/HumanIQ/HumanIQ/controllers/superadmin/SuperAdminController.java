package pfe.HumanIQ.HumanIQ.controllers.superadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Company;
import pfe.HumanIQ.HumanIQ.services.CompanyService;
@RestController
@RequestMapping("/api/company")
public class SuperAdminController {
    @Autowired
    private  CompanyService companyService;




    @PostMapping("/add")
    public ResponseEntity<?> addCompany(@RequestBody Company company) {
        try {
            return ResponseEntity.ok(companyService.addCompany(company));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/edit")
    public ResponseEntity<?> editCompany(@RequestBody Company updatedCompany) {
        try {
            return ResponseEntity.ok(companyService.editCompany(updatedCompany));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCompany() {
        return ResponseEntity.ok(companyService.getCompany());
    }
}
