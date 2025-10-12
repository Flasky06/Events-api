package com.tritva.Evently.model.dto;
import com.tritva.Evently.model.Role;
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

    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
