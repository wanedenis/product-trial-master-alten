package com.alten.ecommerce.repositories;

import com.alten.ecommerce.models.Category;
import com.alten.ecommerce.models.InventoryStatus;
import com.alten.ecommerce.models.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Check if product exists by code
     * @param code the product code
     * @return true if product exists
     */
    boolean existsByCode(String code);

    /**
     * Find products by name containing (case insensitive)
     * @param name the name to search for
     * @return page of products with matching names
     */
    Collection<Product> findByNameContainingIgnoreCase(String name);

    Collection<Product> findByCategory_Name(@NotBlank(message = "Product category is mandatory") String category);
}
