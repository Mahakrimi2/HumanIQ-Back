package pfe.HumanIQ.HumanIQ.services.departmentService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.DepartmentRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepo userRepository;


    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, UserRepo userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }





    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));
    }


    public Department createDepartment(DepartmentName name,Long id) {
        Department department = new Department();
        department.setName(name);
        User responsable = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));
        department.setResponsableDep(responsable);
        return departmentRepository.save(department);
    }




    public Department updateDepartment(Long id, DepartmentName name,Long iduser) {
        System.out.println("Data received from frontend: " + name); // Afficher les données reçues

        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        User responsable = userRepository.findById(iduser).get();
        existingDepartment.setResponsableDep(responsable);
        existingDepartment.setName(name);



        // Sauvegarder le département mis à jour
        Department updatedDepartment = departmentRepository.save(existingDepartment);



        return updatedDepartment;
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


    public Department removeEmployeeFromDepartment(Long departmentId, Long employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        department.removeEmployee(employee);
        return departmentRepository.save(department);
    }
    public List<User> getAvailableEmployees() {
        return userRepository.findByDepartmentIsNull();
    }

    public List<User> getAvailableHeads() {
        List<User> allUsers = userRepository.findAll();
        List<Long> headIds = departmentRepository.findAll().stream()
                .map(department -> Optional.ofNullable(department.getResponsableDep())
                        .map(User::getId)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println(allUsers);

        // Filtrer les utilisateurs qui ne sont pas responsables d'un département
        return allUsers.stream()
                .filter(user -> !headIds.contains(user.getId()))
                .collect(Collectors.toList());

    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}