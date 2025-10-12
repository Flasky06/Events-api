package com.tritva.Evently.model.dto;

import com.tritva.Evently.model.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String location;
    private String county;
    private double price;
    private String imageUrl;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String categoryName;
    private String organiserName;
    private LocalDateTime createdAt;
    private int capacity;
    private long availableTickets;

}
