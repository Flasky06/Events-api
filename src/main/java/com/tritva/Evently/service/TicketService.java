package com.tritva.Evently.service;

import com.tritva.Evently.model.dto.TicketDto;
import com.tritva.Evently.model.dto.TicketRequestDto;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    // Buy or create a new ticket for an event
    TicketDto createTicket(TicketDto ticketDto);

    // Create ticket after successful payment
    TicketDto createTicketAfterPayment(TicketRequestDto request, String email);

    // Get a ticket by its ID
    TicketDto getTicketById(UUID id);

    // List all tickets purchased by a user
    List<TicketDto> getTicketsByUser(UUID userId);

    // List all tickets for a specific event
    List<TicketDto> getTicketsByEvent(UUID eventId);

    // Cancel or delete a ticket
    void deleteTicket(UUID id);

    // Generate QR code for ticket
    String generateQRCode(UUID ticketId);
}