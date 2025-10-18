package com.tritva.Evently.model.dto;

import com.tritva.Evently.model.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateEventDto {
    @NotBlank(message = "Event name is required")
    @Size(min = 3, max = 50, message = "Event name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "County is required")
    private String county;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "Start date and time is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDateTime;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;
}