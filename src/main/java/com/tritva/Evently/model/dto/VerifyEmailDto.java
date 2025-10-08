package com.tritva.assessment.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailDto {
    @NotBlank(message = "Verification token is required")
    private String token;
}
