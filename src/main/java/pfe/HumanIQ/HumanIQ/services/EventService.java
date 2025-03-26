package pfe.HumanIQ.HumanIQ.services;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import pfe.HumanIQ.HumanIQ.models.Event;
import pfe.HumanIQ.HumanIQ.models.User;
import pfe.HumanIQ.HumanIQ.repositories.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(Event event, User creator) {
        event.setCreator(creator);
        return eventRepository.save(event);
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
