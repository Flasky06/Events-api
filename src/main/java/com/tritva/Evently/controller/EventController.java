package com.tritva.Evently.controller;

import com.tritva.Evently.model.dto.CreateEventDto;
import com.tritva.Evently.model.dto.EventResponseDto;
import com.tritva.Evently.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "APIs for managing events")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Create a new event", description = "Creates an event and assigns it to an organiser and category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid event data")
    })
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody CreateEventDto dto) {
        return ResponseEntity.ok(eventService.createEvent(dto));
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
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID id, @RequestBody CreateEventDto dto) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto));
    }

    @Operation(summary = "Delete event", description = "Deletes an event by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
