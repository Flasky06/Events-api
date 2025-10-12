package com.tritva.Evently.model.dto;

import com.tritva.Evently.model.Role;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateEventDto {
    @Size(min = 3, max = 50, message = "Event name must be between 3 and 50 characters")
    String name;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    String description;

    String location;

    String county;

    @Min(value = 0, message = "Price cannot be negative")
    Double price;

    String imageUrl;

    Role role;

    @Future(message = "Start date must be in the future")
    LocalDateTime startDateTime;

    @Future(message = "End date must be in the future")
    LocalDateTime endDateTime;
}
