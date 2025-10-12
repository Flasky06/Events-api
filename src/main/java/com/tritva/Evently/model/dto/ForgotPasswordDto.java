package com.tritva.Evently.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDto {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
}


