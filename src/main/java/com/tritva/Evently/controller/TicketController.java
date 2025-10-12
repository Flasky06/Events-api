package com.tritva.Evently.controller;


import com.tritva.Evently.model.dto.TicketDto;
import com.tritva.Evently.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Buy a ticket", description = "Allows a user to buy a ticket for a specific event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket purchased successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ticket data or event capacity exceeded"),
            @ApiResponse(responseCode = "404", description = "User or event not found")
    })
    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@Valid @RequestBody TicketDto ticketDto) {
        TicketDto createdTicket = ticketService.createTicket(ticketDto);
        return ResponseEntity.ok(createdTicket);
    }

    @Operation(summary = "Get ticket by ID", description = "Fetches details of a single ticket by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket found successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable UUID id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @Operation(summary = "Get user tickets", description = "Fetches all tickets purchased by a given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No tickets found for the given user")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDto>> getTicketsByUser(@PathVariable UUID userId) {
        List<TicketDto> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Delete a ticket", description = "Deletes a ticket by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}

