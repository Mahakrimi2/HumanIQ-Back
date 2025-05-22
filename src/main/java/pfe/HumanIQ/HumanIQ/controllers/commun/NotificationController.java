package pfe.HumanIQ.HumanIQ.controllers.commun;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pfe.HumanIQ.HumanIQ.models.Notification;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.services.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @AuthenticationPrincipal User user) {

        System.out.println("Current authenticated user ID: " + user.getId());
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());

        notifications.forEach(n ->
                System.out.println("Notification " + n.getId() + " - Event: " +
                        (n.getEvent() != null ? n.getEvent().getId() : "null")));

        return ResponseEntity.ok(notifications);

    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(@AuthenticationPrincipal User user) {
        return notificationService.getUnreadUserNotifications(user.getId());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
