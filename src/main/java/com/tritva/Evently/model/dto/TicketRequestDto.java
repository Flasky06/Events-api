package com.tritva.Evently.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TicketRequestDto {

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    // Optional if the buyer isn't logged in
    private UUID userId;

    // Optional if payment will be created separately
    private UUID paymentId;

    @NotNull(message = "Price is required")
    private Double price;

    @NotNull(message = "Phone number is required for STK push")
    @Pattern(
            regexp = "^(?:254|\\+254|0)?7\\d{8}$",
            message = "Invalid Kenyan phone number format"
    )
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;
}