package com.tritva.Evently.model.dto;

import com.tritva.Evently.model.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private UUID id;
    private UUID userId;
    private UUID eventId;
    private double amount;
    private String transactionId;
    private Status status;
    private LocalDateTime createdAt;
}

