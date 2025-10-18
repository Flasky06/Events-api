package com.tritva.Evently.controller;

import com.tritva.Evently.model.dto.CreateEventDto;
import com.tritva.Evently.model.dto.EventResponseDto;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "APIs for managing events")
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @Operation(summary = "Create a new event", description = "Creates an event and assigns it to the logged-in user as organiser.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid event data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @RequestBody CreateEventDto dto,
            Authentication authentication) {

        // Get logged-in user's email
        String email = authentication.getName();

        // Fetch user from database to get their UUID
        User organiser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create event with the logged-in user as organiser
        EventResponseDto createdEvent = eventService.createEvent(dto, organiser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @Operation(summary = "Get all events", description = "Fetches all events in the system.")
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @Operation(summary = "Get event by ID", description = "Retrieve a specific event by its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @Operation(summary = "Search events by name", description = "Search for events whose names contain a given keyword.")
    @GetMapping("/search")
    public ResponseEntity<List<EventResponseDto>> searchEvents(@RequestParam String name) {
        return ResponseEntity.ok(eventService.searchEventsByName(name));
    }

    @Operation(summary = "Get events by category", description = "Fetch all events belonging to a specific category.")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<EventResponseDto>> getEventsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(eventService.getEventsByCategory(categoryId));
    }

    @Operation(summary = "Get events by county", description = "Fetch all events happening in a specific county.")
    @GetMapping("/county/{county}")
    public ResponseEntity<List<EventResponseDto>> getEventsByCounty(@PathVariable String county) {
        return ResponseEntity.ok(eventService.getEventsByCounty(county));
    }

    @Operation(summary = "Update event", description = "Update details of an existing event.")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable UUID id,
            @RequestBody CreateEventDto dto,
            Authentication authentication) {

        // Get logged-in user's email
        String email = authentication.getName();

        // Fetch user from database to get their UUID
        User organiser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update event with the logged-in user as organiser
        EventResponseDto updatedEvent = eventService.updateEvent(id, dto, organiser.getId());

        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Delete event", description = "Deletes an event by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}