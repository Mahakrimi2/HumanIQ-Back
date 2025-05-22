package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.Pointage;
import pfe.HumanIQ.HumanIQ.models.User;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface PointageRepo extends JpaRepository<Pointage, Long> {
    @Query("SELECT p FROM Pointage p WHERE p.user.username = :username")
    List<Pointage> findByUsername(@Param("username") String username);

    int getPointageByWorkingTime(User  user);

    @Query("SELECT SUM(p.workingTime) FROM Pointage p WHERE p.user.id = :userId AND MONTH(p.date) = :month AND YEAR(p.date) = :year")
    Integer getTotalWorkHoursForUserAndMonth(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);


    Optional<Pointage> findByUserUsernameAndDate(String username, LocalDate date);

    List<Pointage> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
