package com.tritva.Evently.controller;

import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;
import com.tritva.Evently.model.dto.TicketDto;
import com.tritva.Evently.model.dto.TicketPurchaseRequestDto;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.TicketPurchaseService;
import com.tritva.Evently.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;
    private final TicketPurchaseService ticketPurchaseService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TicketDto> createTicket(
            @Valid @RequestBody TicketDto ticketDto) {
        TicketDto createdTicket = ticketService.createTicket(ticketDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PostMapping("/purchase")
    public ResponseEntity<MpesaPaymentResponseDto> purchaseTicket(
            @Valid @RequestBody TicketPurchaseRequestDto request,
            Authentication authentication) {

        // Get logged-in user's email
        String email = authentication.getName();

        // Fetch user by email to get their UUID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        log.info("Ticket purchase initiated by user: {} ({})", email, user.getId());

        // Set the user ID from authenticated user
        request.setUserId(user.getId());

        MpesaPaymentResponseDto response = ticketPurchaseService.purchaseTicket(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable UUID id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDto>> getTicketsByUser(@PathVariable UUID userId) {
        List<TicketDto> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketDto>> getMyTickets(Authentication authentication) {
        // Get logged-in user's email
        String email = authentication.getName();

        // Fetch user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<TicketDto> tickets = ticketService.getTicketsByUser(user.getId());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketDto>> getTicketsByEvent(@PathVariable UUID eventId) {
        List<TicketDto> tickets = ticketService.getTicketsByEvent(eventId);
        return ResponseEntity.ok(tickets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<String> getTicketQRCode(@PathVariable UUID id) {
        String qrCode = ticketService.generateQRCode(id);
        return ResponseEntity.ok(qrCode);
    }
}