package pfe.HumanIQ.HumanIQ.models;

import jakarta.persistence.*;

@Entity
public class CompanyRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    // Getters et Setters...
}