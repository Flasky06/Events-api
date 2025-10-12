package com.tritva.Evently.controller;

import com.tritva.Evently.model.dto.CategoryResponseDto;
import com.tritva.Evently.model.dto.CreateCategoryDto;
import com.tritva.Evently.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing event categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Create a new category",
            description = "Adds a new event category (e.g., Music, Tech, Sports)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data")
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CreateCategoryDto createCategoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(createCategoryDto));
    }

    @Operation(
            summary = "List all categories",
            description = "Fetches all event categories available in the system."
    )
    @ApiResponse(responseCode = "200", description = "Categories fetched successfully")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.listCategories());
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieves details for a specific event category by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(
            summary = "Update category",
            description = "Updates the name or description of an existing event category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable UUID id,
            @RequestBody CreateCategoryDto createCategoryDto
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, createCategoryDto));
    }

    @Operation(
            summary = "Delete category",
            description = "Removes a category by its ID. Note: This may affect linked events."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
