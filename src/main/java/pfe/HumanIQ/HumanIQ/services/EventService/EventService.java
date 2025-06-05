package pfe.HumanIQ.HumanIQ.services.EventService;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Event;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.EventRepository;
import pfe.HumanIQ.HumanIQ.repositories.NotificationRepository;
import pfe.HumanIQ.HumanIQ.repositories.UserRepo;
import pfe.HumanIQ.HumanIQ.services.Notifications.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    private final NotificationService notificationService;
    private final UserRepo userRepo;
    private final NotificationRepository notificationRepository;

    public EventService(EventRepository eventRepository, NotificationService notificationService, UserRepo userRepo, NotificationRepository notificationRepository) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.userRepo = userRepo;
        this.notificationRepository = notificationRepository;
    }

    public Event createEvent(Event event, User creator) {
        event.setCreator(creator);
        Event savedEvent = eventRepository.save(event);
        // DEBUG: Comptez les utilisateurs à notifier
        List<User> users = userRepo.findAll();
        System.out.println("Total users: " + users.size());
        // Envoyer les notifications
        notificationService.notifyUsersAboutNewEvent(savedEvent);

        long notificationCount = notificationRepository.count();
        System.out.println("Total notifications after creation: " + notificationCount);

        return savedEvent;
    }

    public Event updateEvent(Long id, Event eventDetails) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException());

        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDateTime(eventDetails.getStartDateTime());
        event.setEndDateTime(eventDetails.getEndDateTime());
        event.setLocation(eventDetails.getLocation());
        event.setType(eventDetails.getType());

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ));
        eventRepository.delete(event);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException());
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartDateTimeBetween(start, end);
    }





}
