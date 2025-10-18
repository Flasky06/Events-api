package com.tritva.Evently.service.impl;

import com.tritva.Evently.mapper.CategoryMapper;
import com.tritva.Evently.model.dto.CategoryResponseDto;
import com.tritva.Evently.model.dto.CreateCategoryDto;
import com.tritva.Evently.model.entity.Category;
import com.tritva.Evently.repository.CategoryRepository;
import com.tritva.Evently.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto createCategoryDto) {
        if (categoryRepository.existsByCategoryName(createCategoryDto.getCategoryName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = categoryMapper.toEntity(createCategoryDto);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> listCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryResponseDto updateCategory(UUID categoryId, CreateCategoryDto createCategoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check if new name already exists (excluding current category)
        if (!category.getCategoryName().equalsIgnoreCase(createCategoryDto.getCategoryName()) &&
                categoryRepository.existsByCategoryName(createCategoryDto.getCategoryName())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        category.setCategoryName(createCategoryDto.getCategoryName());
        Category updated = categoryRepository.save(category);
        return categoryMapper.toDto(updated);
    }

    @Override
    public void deleteCategoryById(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found");
        }
        categoryRepository.deleteById(categoryId);
    }
}
