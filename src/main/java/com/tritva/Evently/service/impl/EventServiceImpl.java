package com.tritva.Evently.service.impl;

import com.tritva.Evently.mapper.EventMapper;
import com.tritva.Evently.model.dto.CreateEventDto;
import com.tritva.Evently.model.dto.EventResponseDto;
import com.tritva.Evently.model.entity.Category;
import com.tritva.Evently.model.entity.Event;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.CategoryRepository;
import com.tritva.Evently.repository.EventRepository;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.EventService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponseDto createEvent(CreateEventDto dto, UUID organiserId) {
        log.info("Creating event: {} by organiser: {}", dto.getName(), organiserId);

        // Fetch organiser
        User organiser = userRepository.findById(organiserId)
                .orElseThrow(() -> new EntityNotFoundException("Organiser not found"));

        // Fetch category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        // Validate dates
        if (dto.getEndDateTime().isBefore(dto.getStartDateTime())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Map DTO to entity
        Event event = eventMapper.toEntity(dto);
        event.setOrganiser(organiser);
        event.setCategory(category);

        // Save event
        Event savedEvent = eventRepository.save(event);

        log.info("Event created successfully with ID: {} and capacity: {}",
                savedEvent.getId(), savedEvent.getCapacity());

        return eventMapper.toDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseDto getEventById(UUID id) {
        log.info("Fetching event with ID: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));

        EventResponseDto dto = eventMapper.toDto(event);
        log.info("Event {} has {} available tickets out of {} capacity",
                event.getName(), dto.getAvailableTickets(), event.getCapacity());

        return dto;
    }

    @Override
    @Transactional
    public EventResponseDto updateEvent(UUID id, CreateEventDto dto, UUID organiserId) {
        log.info("Updating event with ID: {}", id);

        // Fetch existing event
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Fetch category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        // Fetch organiser
        User organiser = userRepository.findById(organiserId)
                .orElseThrow(() -> new EntityNotFoundException("Organiser not found"));

        // Validate dates
        if (dto.getEndDateTime().isBefore(dto.getStartDateTime())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check if new capacity is valid (cannot be less than tickets sold)
        int ticketsSold = event.getTickets().size();
        if (dto.getCapacity() < ticketsSold) {
            throw new IllegalArgumentException(
                    String.format("Cannot reduce capacity to %d. %d tickets already sold.",
                            dto.getCapacity(), ticketsSold)
            );
        }

        // Update event fields
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setCounty(dto.getCounty());
        event.setPrice(dto.getPrice());
        event.setImageUrl(dto.getImageUrl());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setCategory(category);
        event.setOrganiser(organiser);
        event.setCapacity(dto.getCapacity());

        // Save updated event
        Event updatedEvent = eventRepository.save(event);

        log.info("Event updated successfully. New capacity: {}, Available: {}",
                updatedEvent.getCapacity(), updatedEvent.getAvailableTickets());

        return eventMapper.toDto(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id) {
        log.info("Deleting event with ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Check if tickets have been sold
        if (!event.getTickets().isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete event. %d tickets have been sold.",
                            event.getTickets().size())
            );
        }

        eventRepository.delete(event);
        log.info("Event deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> searchEventsByName(String name) {
        log.info("Searching events by name: {}", name);
        return eventRepository.findByNameContainingIgnoreCase(name).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByCategory(UUID categoryId) {
        log.info("Fetching events for category: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return eventRepository.findByCategory(category).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByCounty(String county) {
        log.info("Fetching events for county: {}", county);
        return eventRepository.findByCountyIgnoreCase(county).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }
}
