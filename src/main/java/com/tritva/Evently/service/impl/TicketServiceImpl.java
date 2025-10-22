package com.tritva.Evently.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tritva.Evently.mapper.EventMapper;
import com.tritva.Evently.mapper.TicketMapper;
import com.tritva.Evently.mapper.UserMapper;
import com.tritva.Evently.model.dto.TicketDto;
import com.tritva.Evently.model.dto.TicketRequestDto;
import com.tritva.Evently.model.entity.Event;
import com.tritva.Evently.model.entity.Payment;
import com.tritva.Evently.model.entity.Ticket;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.EventRepository;
import com.tritva.Evently.repository.PaymentRepository;
import com.tritva.Evently.repository.TicketRepository;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.EmailService;
import com.tritva.Evently.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Value("${app.qr.storage.path:qrcodes}")
    private String qrStoragePath;

    @Override
    public synchronized TicketDto createTicket(TicketDto ticketDto) {
        log.info("Creating ticket for event {} by user {}", ticketDto.getEventId(), ticketDto.getUserId());

        // Fetch user and event
        User user = userRepository.findById(ticketDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + ticketDto.getUserId()));

        Event event = eventRepository.findById(ticketDto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + ticketDto.getEventId()));

        // Check if event has available tickets
        if (event.isSoldOut()) {
            log.warn("Event {} is sold out. Capacity: {}, Tickets sold: {}",
                    event.getName(), event.getCapacity(), event.getTickets().size());
            throw new RuntimeException("Tickets for this event are sold out!");
        }

        log.info("Event {} has {} tickets available", event.getName(), event.getAvailableTickets());

        // Map to entity
        Ticket ticket = ticketMapper.toEntity(ticketDto);
        ticket.setUser(user);
        ticket.setEvent(event);

        // Generate ticket number and verification code
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setVerificationCode(generateVerificationCode());

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Generate QR code
        generateQRCode(savedTicket.getId());

        log.info("Ticket created successfully. Remaining tickets: {}", event.getAvailableTickets());

        // Return mapped DTO
        return ticketMapper.toDto(savedTicket);
    }

    @Override
    public synchronized TicketDto createTicketAfterPayment(TicketRequestDto request, String email) {
        log.info("Creating ticket after payment for event {}", request.getEventId());

        // Fetch event
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + request.getEventId()));

        // Check if event has available tickets
        if (event.isSoldOut()) {
            log.error("Attempted to create ticket for sold out event: {}", event.getName());
            throw new RuntimeException("Tickets for this event are sold out!");
        }

        log.info("Creating ticket. Event {} has {} tickets available out of {} capacity",
                event.getName(), event.getAvailableTickets(), event.getCapacity());

        // Fetch user if userId is provided
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + request.getUserId()));
        }

        // Fetch payment if paymentId is provided
        Payment payment = null;
        if (request.getPaymentId() != null) {
            payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found with ID: " + request.getPaymentId()));
        }

        // Create ticket
        Ticket ticket = Ticket.builder()
                .event(event)
                .user(user)
                .payment(payment)
                .price(request.getPrice())
                .ticketNumber(generateTicketNumber())
                .verificationCode(generateVerificationCode())
                .checkedIn(false)
                .build();

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Generate QR code
        String qrCodeUrl = generateQRCode(savedTicket.getId());
        savedTicket.setQrCodeUrl(qrCodeUrl);
        ticketRepository.save(savedTicket);

        // Send ticket via email
        emailService.sendTicketEmail(email, savedTicket);

        log.info("Ticket created successfully with number: {}. Remaining tickets: {}",
                savedTicket.getTicketNumber(), event.getAvailableTickets());

        return ticketMapper.toDto(savedTicket);
    }

    @Override
    public TicketDto getTicketById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        log.info("Fetched ticket {} for event {} by user {}",
                ticket.getTicketNumber(),
                ticket.getEvent().getName(),
                ticket.getUser() != null ? ticket.getUser().getFullName() : "Guest");

        return ticketMapper.toDto(ticket);
    }

    @Override
    public List<TicketDto> getTicketsByUser(UUID userId) {
        log.info("Fetching tickets for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        return tickets.stream()
                .map(ticketMapper::toDto)
                .toList();
    }

    @Override
    public List<TicketDto> getTicketsByEvent(UUID eventId) {
        log.info("Fetching tickets for Event {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        log.info("Event {} has {} tickets sold out of {} capacity",
                event.getName(), tickets.size(), event.getCapacity());

        return tickets.stream()
                .map(ticketMapper::toDto)
                .toList();
    }

    @Override
    public void deleteTicket(UUID id) {
        log.info("Deleting ticket with ID: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        Event event = ticket.getEvent();
        ticketRepository.delete(ticket);

        log.info("Ticket {} deleted successfully. Event {} now has {} available tickets",
                id, event.getName(), event.getAvailableTickets());
    }

    @Override
    public String generateQRCode(UUID ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

            // QR code content: ticket verification URL
            String qrContent = String.format("TICKET:%s:CODE:%s",
                    ticket.getTicketNumber(),
                    ticket.getVerificationCode());

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            // Convert to Base64
            String base64QR = Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
            String qrCodeUrl = "data:image/png;base64," + base64QR;

            // Update ticket with QR code URL
            ticket.setQrCodeUrl(qrCodeUrl);
            ticketRepository.save(ticket);

            log.info("QR code generated for ticket: {}", ticket.getTicketNumber());

            return qrCodeUrl;

        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }

    private String generateTicketNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TKT-" + timestamp + "-" + random;
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}