package pfe.HumanIQ.HumanIQ.controllers.rh;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Department;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.services.departmentService.DepartmentService;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/rh")
@CrossOrigin(origins = "http://localhost:4300")
public class DepartementController {
    private final DepartmentService departmentService;

    public DepartementController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/department/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        return department.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/department")
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        try {
            Department createdDepartment = departmentService.createDepartment(department);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/department/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        try {
            Department updatedDepartment = departmentService.updateDepartment(id, department);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
}
