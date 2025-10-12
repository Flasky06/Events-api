package com.tritva.Evently.service;

import com.tritva.Evently.model.dto.CategoryResponseDto;
import com.tritva.Evently.model.dto.CreateCategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    // Create a new category
    CategoryResponseDto createCategory(CreateCategoryDto createCategoryDto);

    // List all categories
    List<CategoryResponseDto> listCategories();

    // Get a single category by ID
    CategoryResponseDto getCategoryById(UUID categoryId);

    // Update an existing category
    CategoryResponseDto updateCategory(UUID categoryId, CreateCategoryDto createCategoryDto);

    // Delete a category by ID
    void deleteCategoryById(UUID categoryId);
}
