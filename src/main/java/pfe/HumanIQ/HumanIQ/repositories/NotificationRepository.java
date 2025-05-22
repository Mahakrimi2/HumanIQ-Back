package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pfe.HumanIQ.HumanIQ.models.Notification;
import pfe.HumanIQ.HumanIQ.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

   // List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);
   @EntityGraph(attributePaths = {"event", "recipient"})
   @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId")
   List<Notification> findNotificationsWithEvent(@Param("userId") Long userId);
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);


    boolean existsByEventIdAndRecipientIdAndCreatedAtAfter(
            Long eventId,
            Long recipientId,
            LocalDateTime createdAt);

    List<Notification> findByRecipient(User recipient);
}
