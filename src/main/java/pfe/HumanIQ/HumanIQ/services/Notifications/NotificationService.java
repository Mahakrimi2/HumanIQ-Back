package pfe.HumanIQ.HumanIQ.services.Notifications;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.*;
import pfe.HumanIQ.HumanIQ.repositories.NotificationRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepo userRepository;


    public NotificationService(NotificationRepository notificationRepository, UserRepo userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyUsersAboutNewEvent(Event event) {

        List<User> usersToNotify = userRepository.findAll()
                .stream()
                .filter(user -> !user.getId().equals(event.getCreator().getId()))
                .collect(Collectors.toList());

        for (User user : usersToNotify) {
            Notification notification = new Notification();
            notification.setMessage("new Event Created: " + event.getTitle());
            notification.setRecipient(user);
            notification.setEvent(event);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now()); // Date de création

            notificationRepository.saveAndFlush(notification);
        }
    }
    public void notifyAboutHolidayRequest(Holiday holiday) {
        if (holiday.getStatus() != HolidayStatus.PENDING) {
            System.out.println("Holiday status is not PENDING, skipping notification.");
            return;
        }
        System.out.println("Fetching users to notify...");
        // Filtering users with roles ROLE_RH or ROLE_SUPERADMIN
        List<User> usersToNotify = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .map(Role::getName) // Get role name from Role entity
                        .anyMatch(role -> role.equals(UserRole.ROLE_RH) || role.equals(UserRole.ROLE_SUPERADMIN))) // Check against UserRole enum
                .collect(Collectors.toList());



        String message = String.format(
                "New Holiday Request de %s - Du %s (%d jours) - Motif: %s",
                holiday.getUser().getFullname(),
                holiday.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                holiday.getDuration(),
                holiday.getReason()
        );
        System.out.println("Notification message: " + message);
        List<Notification> notifications = usersToNotify.stream()
                .map(user -> {
                    System.out.println("Creating notification for: " + user.getFullname());
                    Notification notification = new Notification();
                    notification.setMessage(message);
                    notification.setRecipient(user);
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    return notification;
                })
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
        System.out.println("Notifications saved: " + notifications.size());
    }



    public void notifyAboutContractRenewal(Contract contract) {
        // Vérifier si le contrat arrive à expiration dans moins de 30 jours


        LocalDate now = LocalDate.now();
        if (contract.getEndDate().isAfter(now.plusDays(1))) {
            return; // Trop tôt pour notifier
        }


        System.out.println("\n=== DEBUT notifyAboutContractRenewal ===");
        System.out.println("Contrat ID: " + contract.getId());
        System.out.println("Employé: " + contract.getUser().getFullname());
        System.out.println("Date fin: " + contract.getEndDate());
        System.out.println("Type: " + contract.getContractType());

        String message = String.format(
                "\n" +
                        "Contract to be renewed: %s - Expires on %s",
                contract.getUser().getFullname(),
                contract.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        Notification employeeNotification = new Notification();
        employeeNotification.setMessage(message);
        employeeNotification.setRecipient(contract.getUser());
        employeeNotification.setRead(false);
        employeeNotification.setCreatedAt(LocalDateTime.now());


        System.out.println("Message employé: " + message);


        // 2. Notifications pour les RH
        System.out.println("\nRecherche des RH à notifier...");
        // Notification pour les RH
        List<User> hrUsers = userRepository.findAll().stream()
                .filter(user ->

                        user.getRoles().stream()

                        .map(Role::getName)
                        .anyMatch(role -> role.equals(UserRole.ROLE_RH) || role.equals(UserRole.ROLE_SUPERADMIN)))
                .collect(Collectors.toList());

        System.out.println("Vérification user: " + hrUsers.get(1).getFullname());

        List<Notification> notifications = new ArrayList<>();
        notifications.add(employeeNotification);

        hrUsers.forEach(hrUser -> {
            Notification hrNotification = new Notification();
            hrNotification.setMessage(message + " (Employee: " + contract.getUser().getFullname() + ")");
            hrNotification.setRecipient(hrUser);
            hrNotification.setRead(false);
            hrNotification.setCreatedAt(LocalDateTime.now());
            notifications.add(hrNotification);
        });


        System.out.println("\nSauvegarde des notifications...");

        notificationRepository.saveAll(notifications);


        System.out.println("\nSauvegarde des notifications...");
    }

    @Transactional()
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    public List<Notification> getUnreadUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }


    public boolean hasRecentContractNotification(Long contractId, Long userId, LocalDateTime since) {
        return notificationRepository.existsByEventIdAndRecipientIdAndCreatedAtAfter(
                contractId,
                userId,
                since
        );
    }
}
