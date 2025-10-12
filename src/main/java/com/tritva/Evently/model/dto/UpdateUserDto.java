package com.tritva.Evently.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String fullName;

}

