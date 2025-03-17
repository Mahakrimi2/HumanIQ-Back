package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pfe.HumanIQ.HumanIQ.models.Contract;
import pfe.HumanIQ.HumanIQ.models.Holiday;
import pfe.HumanIQ.HumanIQ.models.User;

import java.time.YearMonth;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT h FROM Holiday h JOIN FETCH h.user")
    List<Holiday> findAllWithUser();
    @Query("SELECT h FROM Holiday h WHERE h.user.username = :username")
    List<Holiday> findByUsername(@Param("username") String username);




    @Query("SELECT COUNT(h) FROM Holiday h WHERE h.user.id = :userId AND h.status = 'ACCEPTED'  AND MONTH(h.startDate) = :month AND YEAR(h.startDate) = :year")
    int getAcceptedHolidaysForUserAndMonth(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
    @Query("SELECT h.duration FROM Holiday h WHERE h.user.id = :userId AND h.status = 'ACCEPTED' AND h.type = 'ANNUAL' AND FUNCTION('MONTH', h.startDate) = :month AND FUNCTION('YEAR', h.startDate) = :year")
    int getHolidayByTypeAndUser(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

}
