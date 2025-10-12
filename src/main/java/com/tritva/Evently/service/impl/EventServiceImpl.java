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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponseDto createEvent(CreateEventDto dto) {
        User organiser = userRepository.findById(dto.getOrganiserId())
                .orElseThrow(() -> new EntityNotFoundException("Organiser not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Event event = eventMapper.toEntity(dto);
        event.setOrganiser(organiser);
        event.setCategory(category);

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved);
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDto getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        return eventMapper.toDto(event);
    }

    @Override
    public EventResponseDto updateEvent(UUID id, CreateEventDto dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        User organiser = userRepository.findById(dto.getOrganiserId())
                .orElseThrow(() -> new EntityNotFoundException("Organiser not found"));

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

        Event updated = eventRepository.save(event);
        return eventMapper.toDto(updated);
    }

    @Override
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found");
        }
        eventRepository.deleteById(id);
    }

    @Override
    public List<EventResponseDto> searchEventsByName(String name) {
        return eventRepository.findByNameContainingIgnoreCase(name).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDto> getEventsByCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return eventRepository.findByCategory(category).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDto> getEventsByCounty(String county) {
        return eventRepository.findByCountyIgnoreCase(county).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }
}
