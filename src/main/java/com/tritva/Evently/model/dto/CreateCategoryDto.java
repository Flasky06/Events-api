package com.tritva.Evently.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDto {
    @NotBlank(message = "Category name is required")
    private String categoryName;
}
