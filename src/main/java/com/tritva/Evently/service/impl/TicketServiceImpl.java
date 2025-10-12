package com.tritva.Evently.service.impl;

import com.tritva.Evently.mapper.EventMapper;
import com.tritva.Evently.mapper.TicketMapper;
import com.tritva.Evently.mapper.UserMapper;
import com.tritva.Evently.model.dto.TicketDto;
import com.tritva.Evently.model.entity.Event;
import com.tritva.Evently.model.entity.Ticket;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.EventRepository;
import com.tritva.Evently.repository.TicketRepository;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private  final UserMapper userMapper;
    private  final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        log.info("Creating ticket for event {} by user {}", ticketDto.getEventId(), ticketDto.getUserId());

        // Fetch user and event
        User user = userRepository.findById(ticketDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + ticketDto.getUserId()));

        Event event = eventRepository.findById(ticketDto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + ticketDto.getEventId()));

        // Check event capacity
        long ticketsSold = ticketRepository.countByEventId(event.getId());
        if (ticketsSold >= event.getCapacity()) {
            throw new RuntimeException("Tickets for this event are sold out!");
        }

        // Map to entity
        Ticket ticket = ticketMapper.toEntity(ticketDto);
        ticket.setUser(user);
        ticket.setEvent(event);

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Return mapped DTO
        return ticketMapper.toDto(savedTicket);
    }



    @Override
    public TicketDto getTicketById(UUID id) {
        // Try to find the ticket
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        // Log the result for debugging
        log.info("Fetched ticket {} for event {} by user {}",
                ticket.getTicketNumber(),
                ticket.getEvent().getName(),
                ticket.getUser().getFullName());

        // Map to DTO and return
        return ticketMapper.toDto(ticket);
    }


    @Override
    public List<TicketDto> getTicketsByUser(UUID userId) {
        log.info("Fetching tickets for user {}", userId);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Fetch tickets belonging to the user
        List<Ticket> tickets = ticketRepository.findByUser(user);

        // Map to DTO list and return
        return tickets.stream()
                .map(ticketMapper::toDto)
                .toList();
    }


    @Override
    public List<TicketDto> getTicketsByEvent(UUID eventId) {
        log.info("Fetching tickets for Event {}", eventId);

        // Check if Event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Fetch tickets belonging to the Event
        List<Ticket> tickets = ticketRepository.findByEvent(event);

        return tickets.stream()
                .map(ticketMapper::toDto)
                .toList();
    }

    @Override
    public void deleteTicket(UUID id) {
        log.info("Deleting ticket with ID: {}", id);

        // Check if ticket exists
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + id));

        // Delete the ticket
        ticketRepository.delete(ticket);

        log.info("Ticket {} deleted successfully", id);
    }

}
