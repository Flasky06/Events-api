package com.tritva.Evently.model.dto;

import com.tritva.Evently.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String email;
    private Role role;
    private UUID userId;
    private String message;
}