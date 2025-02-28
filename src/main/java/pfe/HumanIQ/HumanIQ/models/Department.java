package pfe.HumanIQ.HumanIQ.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private User responsableDep;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<User> employees;



    public User getResponsableDep() {
        return responsableDep;
    }

    public void setResponsableDep(User responsableDep) {
        if (responsableDep != null && responsableDep.getDepartment() != null && !responsableDep.getDepartment().equals(this)) {
            throw new IllegalStateException("Cet utilisateur est déjà responsable d'un autre département.");
        }
        this.responsableDep = responsableDep;
    }
    public void addEmployee(User employee) {
        if (employee != null && !employees.contains(employee)) {
            employee.setDepartment(this);
        }
    }

    public void removeEmployee(User employee) {
        if (employee != null && employees.contains(employee)) {
            employee.setDepartment(null);
            employees.remove(employee);
        }
    }
    public List<User> getUsers() {
        return employees;
    }

    public void setUsers(List<User> employees) {
        this.employees = employees;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }





}