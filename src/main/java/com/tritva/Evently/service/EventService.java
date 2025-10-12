package com.tritva.Evently.service;

import com.tritva.Evently.model.dto.CreateEventDto;
import com.tritva.Evently.model.dto.EventResponseDto;

import java.util.List;
import java.util.UUID;

public interface EventService {
    EventResponseDto createEvent(CreateEventDto createEventDto);
    List<EventResponseDto> getAllEvents();
    EventResponseDto getEventById(UUID id);
    EventResponseDto updateEvent(UUID id, CreateEventDto createEventDto);
    void deleteEvent(UUID id);
    List<EventResponseDto> searchEventsByName(String name);
    List<EventResponseDto> getEventsByCategory(UUID categoryId);
    List<EventResponseDto> getEventsByCounty(String county);
}
