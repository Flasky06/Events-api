package com.tritva.Evently.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private UUID eventId;
    private BigDecimal amount;
    private String phoneNumber;
}
