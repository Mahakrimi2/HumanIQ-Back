package pfe.HumanIQ.HumanIQ.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private double salary;

    private LocalDate generatedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public Payslip setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public Payslip setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public double getSalary() {
        return salary;
    }

    public Payslip setSalary(double salary) {
        this.salary = salary;
        return this;
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public Payslip setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Payslip setUser(User user) {
        this.user = user;
        return this;
    }
}
