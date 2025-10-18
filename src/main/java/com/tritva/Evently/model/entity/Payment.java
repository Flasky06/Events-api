package com.tritva.Evently.model.entity;

import com.tritva.Evently.model.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Who made the payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable so guests can also pay

    // Which event the payment is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Unique transaction or checkout ID from Mpesa
    @Column(unique = true)
    private String mpesaCheckoutRequestId;

    // Mpesa transaction result description
    private String mpesaResultDesc;

    // Phone number used to make payment
    private String phoneNumber;

    // Mpesa transaction ID (for confirmed payments)
    private String transactionId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
