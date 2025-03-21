package pfe.HumanIQ.HumanIQ.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Company;
import pfe.HumanIQ.HumanIQ.repositories.CompanyRepository;

import java.util.Optional;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;

    public Company addCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }

        try {
            Company newCompany = companyRepository.save(company);
            return newCompany;
        } catch (Exception e) {
            // You can log the error if needed or throw a custom exception
            throw new RuntimeException("Error while saving company", e);
        }
    }


    public Company editCompany(Company updatedCompany) {
        Company existingCompany = companyRepository.findById(updatedCompany.getId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));

        existingCompany.setName(updatedCompany.getName());
        existingCompany.setDescription(updatedCompany.getDescription());
        existingCompany.setMission(updatedCompany.getMission());
        existingCompany.setVision(updatedCompany.getVision());
        existingCompany.setValues(updatedCompany.getValues());
        existingCompany.setFoundedYear(updatedCompany.getFoundedYear());
        existingCompany.setLocation(updatedCompany.getLocation());
        existingCompany.setWebsite(updatedCompany.getWebsite());
        existingCompany.setPolicies(updatedCompany.getPolicies());

        return companyRepository.save(existingCompany);
    }

    // Récupérer l'entreprise
    public Optional<Company> getCompany() {
        return companyRepository.findById(1L);
    }
}
