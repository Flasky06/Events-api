package com.tritva.Evently.repository;

import com.tritva.Evently.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByUserId(UUID userId);

    List<Ticket> findByEventId(UUID eventId);

    Optional<Ticket> findByVerificationCode(String verificationCode);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    boolean existsByPaymentId(UUID paymentId);

    long countByEventId(UUID eventId);
}