package com.tritva.Evently.mapper;

import com.tritva.Evently.model.dto.CategoryResponseDto;
import com.tritva.Evently.model.dto.CreateCategoryDto;
import com.tritva.Evently.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {
    CategoryResponseDto toDto(Category category);
    Category toEntity(CreateCategoryDto createCategoryDto);
}
