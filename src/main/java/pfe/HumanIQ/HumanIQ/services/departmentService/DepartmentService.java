package pfe.HumanIQ.HumanIQ.services.departmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.DepartmentRepository;
import pfe.HumanIQ.HumanIQ.repositories.RoleRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.util.Arrays;
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
    private RoleRepository roleRepository;

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
        if (id != null && id > 0) {
            User responsable = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));
            department.setResponsableDep(responsable);
        } else {
            department.setResponsableDep(null);
        }
        return departmentRepository.save(department);
    }



    public Department updateDepartment(Long id, Department departmentName, Long iduser) {
        System.out.println("Data received from frontend: " + departmentName.getName() + ", Responsible user ID: " + iduser);

        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (departmentName.getName() != null ) {
            existingDepartment.setName(departmentName.getName());
        }

        if (iduser != null) {
            User responsableDep = userRepository.findById(iduser)
                    .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));

            // Vérification si l'utilisateur est déjà responsable d'un autre département
            if (responsableDep.getDepartments() != null && !responsableDep.getDepartments().isEmpty()) {
                throw new IllegalStateException("Cet utilisateur est déjà responsable d'un autre département.");
            }

            existingDepartment.setResponsableDep(responsableDep);
        }

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

        Role role = roleRepository.findByName(UserRole.ROLE_EMPLOYEE);
        return userRepository.findByRoles(role);
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


    public Department getDepartmentByName(DepartmentName departmentName) {
        return departmentRepository.findByName(departmentName)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));
    }



}
