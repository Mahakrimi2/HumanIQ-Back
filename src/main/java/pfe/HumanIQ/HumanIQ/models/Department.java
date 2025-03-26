package pfe.HumanIQ.HumanIQ.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Enumerated(EnumType.STRING)
    private DepartmentName name;
    @ManyToOne
    private User responsableDep;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<User> employees;


    public User getResponsableDep() {
        return responsableDep;
    }

    public void setResponsableDep(User responsableDep) {
        if (responsableDep != null) {
            if (responsableDep.getDepartments() != null && !responsableDep.getDepartments().isEmpty()
                    && !responsableDep.getDepartments().contains(this)) {
                throw new IllegalStateException("Cet utilisateur est déjà responsable d'un autre département.");
            }
        }
        this.responsableDep = responsableDep;
    }

    public void addEmployee(User employee) {
        if (employee != null && !employees.contains(employee)) {
            employee.setDepartment(this);
            employees.add(employee);
        }
    }


    public void removeEmployee(User employee) {
        if (employee != null && employees.contains(employee)) {
            employee.setDepartment(null);
            employees.remove(employee);
        }
    }

    public DepartmentName getName() {
        return name;
    }

    public void setName(DepartmentName name) {
        this.name = name;
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



}
