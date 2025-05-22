package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pfe.HumanIQ.HumanIQ.models.Event;
import pfe.HumanIQ.HumanIQ.models.EventType;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);


    @Query("SELECT e FROM Event e WHERE e.creator.id = :userId")
    List<Event> findByCreator(@Param("userId") Long userId);

    @Query("SELECT e FROM Event e WHERE e.type = :type AND e.startDateTime >= :startDate")
    List<Event> findByTypeAndStartDateAfter(
            @Param("type") EventType type,
            @Param("startDate") LocalDateTime startDate);



}
