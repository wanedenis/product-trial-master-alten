package com.alten.ecommerce.repositories;

import com.alten.ecommerce.models.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(@NotNull(message = "Name is mandatory") @Size(max = 100, message = "Category must not exceed 100 characters") String name);

}
