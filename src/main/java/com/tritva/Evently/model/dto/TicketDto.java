package com.tritva.Evently.model.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {
    private UUID id;

    private UUID eventId;
    private UUID userId;
    private UUID paymentId;

    private String ticketNumber;
    private Double price;
    private boolean checkedIn;

    private String qrCodeUrl;
    private String verificationCode;

    private LocalDateTime createdAt;
}
