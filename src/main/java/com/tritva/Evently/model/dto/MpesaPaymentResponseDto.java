package com.tritva.Evently.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MpesaPaymentResponseDto {
    private String checkoutRequestId;
    private String message;
}
