package pfe.HumanIQ.HumanIQ.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pfe.HumanIQ.HumanIQ.models.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface UserRepo extends JpaRepository<User,Long>{
    Optional<User> findByUsername(String username);
    // Trouver les utilisateurs qui ne sont pas responsables d'un département
    List<User> findByDepartmentIsNull();
    List<User> findByRoles(Role role);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();
    List<User> findByDepartment(DepartmentName departmentName);
    @Modifying
    @Query("UPDATE User u SET u.department = NULL WHERE u.department.id = :departmentId")
    void detachUsersFromDepartment(@Param("departmentId") Long departmentId);



}
