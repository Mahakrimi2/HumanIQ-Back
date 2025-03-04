package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pfe.HumanIQ.HumanIQ.models.Holiday;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT h FROM Holiday h JOIN FETCH h.user")
    List<Holiday> findAllWithUser();

}
