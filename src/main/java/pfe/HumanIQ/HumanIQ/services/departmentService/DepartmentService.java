package pfe.HumanIQ.HumanIQ.services.departmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.DepartmentRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepo userRepository;


    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department createDepartment(Department department) {
        // Récupérer l'utilisateur responsable à partir de son ID
        if (department.getResponsableDep() != null && department.getResponsableDep().getId() != null) {
            User responsable = userRepository.findById(department.getResponsableDep().getId())
                    .orElseThrow(() -> new RuntimeException("Responsable not found"));
            department.setResponsableDep(responsable); // Assigner l'utilisateur récupéré
        }

        return departmentRepository.save(department);
    }



    public Department updateDepartment(Long id, Department departmentDetails) {
        return departmentRepository.findById(id).map(department -> {
            department.setName(departmentDetails.getName());
            if (departmentDetails.getResponsableDep() != null) {
                User newResponsable = departmentDetails.getResponsableDep();
                if (newResponsable.getDepartment() != null && !newResponsable.getDepartment().equals(department)) {
                    throw new RuntimeException("Le nouveau responsable est déjà assigné à un autre département.");
                }
                department.setResponsableDep(newResponsable);
            }

            return departmentRepository.save(department);
        }).orElseThrow(() -> new RuntimeException("Département non trouvé"));
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    public Department addEmployeeToDepartment(Long departmentId, Long employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        if (employee.getDepartment() != null && !employee.getDepartment().equals(department)) {
            throw new RuntimeException("L'employé est déjà dans un autre département.");
        }

        department.addEmployee(employee);
        return departmentRepository.save(department);
    }

    // Retirer un employé d'un département
    public Department removeEmployeeFromDepartment(Long departmentId, Long employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // Retirer l'employé du département
        department.removeEmployee(employee);
        return departmentRepository.save(department);
    }

    // Récupérer la liste des employés disponibles (non assignés à un département)
    public List<User> getAvailableEmployees() {
        return userRepository.findByDepartmentIsNull();
    }
}