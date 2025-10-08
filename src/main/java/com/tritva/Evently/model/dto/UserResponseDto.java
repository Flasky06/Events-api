package com.tritva.assessment.model.dto;

import com.tritva.assessment.model.UserRole;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {

    private UUID id;
    private String email;

    private String firstName;
    private String middleName;
    private String sirName;
    private String nationality;

    private UserRole role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
