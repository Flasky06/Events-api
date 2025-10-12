package com.tritva.Evently.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MpesaPaymentRequestDto {
    private String phone;
    private BigDecimal amount;
    private UUID eventId;
}
