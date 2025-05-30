package pfe.HumanIQ.HumanIQ.controllers.rh;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.DepartmentName;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.DepartmentRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.departmentService.DepartmentService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/rh")
//@CrossOrigin(origins = "http://localhost:4400")
public class DepartementController {
    private final DepartmentService departmentService;
    private final UserRepo userRepo;
    private final DepartmentRepository departmentRepository;

    public DepartementController(DepartmentService departmentService, UserRepo userRepo,
                                 DepartmentRepository departmentRepository) {
        this.departmentService = departmentService;
        this.userRepo = userRepo;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }
    @GetMapping("/departments/names")
    public List<String> getDepartmentsNames() {
        return Arrays.stream(DepartmentName.values())
                .map(Enum::name)
                .toList();
    }

    @GetMapping("/department/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = Optional.ofNullable(departmentService.getDepartmentById(id));
        return department.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/departments/{id}/{department}")
    public ResponseEntity<?> createDepartment(@PathVariable DepartmentName department,@PathVariable(required = false) String id) {
        try {
            Long convertedId = null;
            if (id != null && !id.equals("undefined") && !id.isEmpty()) {
                convertedId = Long.parseLong(id);
            }
            Optional<Department> existingDepartment = departmentRepository.findByName(department);
            if (existingDepartment.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le département avec ce nom existe déjà");
            }

            Department createdDepartment = departmentService.createDepartment(department,convertedId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Department department,@RequestParam(value = "iduser",required = false) String iduser) {
        try {
            Long idUserLong = null;
            if (iduser != null && !iduser.equals("NaN") && !iduser.isEmpty()) {
                idUserLong = Long.parseLong(iduser);
            }
            Department updatedDepartment = departmentService.updateDepartment(id, department,idUserLong);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/department/names")
    public List<DepartmentName> getDepartmentNames() {
        return Arrays.asList(DepartmentName.values());
    }

    @DeleteMapping("/department/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/department/{departmentId}/add-employee")
    public ResponseEntity<?> addEmployeeToDepartment(@PathVariable Long departmentId, @RequestParam Long employeeId) {
        try {
            Department department = departmentService.addEmployeeToDepartment(departmentId, employeeId);
            return ResponseEntity.ok(department);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/department/{departmentId}/remove-employee")
    public ResponseEntity<?> removeEmployeeFromDepartment(@PathVariable Long departmentId, @RequestParam Long employeeId) {
        try {
            Department department = departmentService.removeEmployeeFromDepartment(departmentId, employeeId);
            return ResponseEntity.ok(department);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/department/available-employees")
    public ResponseEntity<List<User>> getAvailableEmployees() {
        List<User> availableEmployees = departmentService.getAvailableEmployees();
        return ResponseEntity.ok(availableEmployees);
    }
    @GetMapping("/department/available-heads")
    public List<User> getAvailableHeads() {
        return departmentService.getAvailableHeads();
    }

    @GetMapping("/department/name/{departmentName}")
    public ResponseEntity<?> getDepartmentByName(@PathVariable DepartmentName departmentName) {
        try {
            // Récupérer le département par son nom
            Department department = (Department) departmentService.getDepartmentByName(departmentName);

            // Retourner le département avec ses employés associés
            return ResponseEntity.ok(department);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}
