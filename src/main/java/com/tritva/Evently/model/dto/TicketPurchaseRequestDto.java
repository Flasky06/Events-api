package com.tritva.Evently.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketPurchaseRequestDto {

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    // Optional - only if user is logged in
    private UUID userId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:254|\\+254|0)?7\\d{8}$",
            message = "Invalid Kenyan phone number format"
    )
    private String phoneNumber;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}