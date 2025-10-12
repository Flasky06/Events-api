package com.tritva.Evently.repository;

import com.tritva.Evently.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Find category by name (case-insensitive)
    Optional<Category> findByCategoryNameIgnoreCase(String name);

    // Check if a category name already exists
    boolean existsByCategoryName(String categoryName);
}
