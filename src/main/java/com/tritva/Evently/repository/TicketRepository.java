package com.tritva.Evently.repository;

import com.tritva.Evently.model.entity.Event;
import com.tritva.Evently.model.entity.Ticket;
import com.tritva.Evently.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    // Find all tickets for a specific event
    List<Ticket> findByEvent(Event event);

    // Find tickets for a specific event and user
    List<Ticket> findByEventAndUser(Event event, User user);

    // Get all tickets purchased by a specific user
    List<Ticket> findByUser(User user);

    // Count tickets sold for one event
    long countByEventId(UUID eventId);
}