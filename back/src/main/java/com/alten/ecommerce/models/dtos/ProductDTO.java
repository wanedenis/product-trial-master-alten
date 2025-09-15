package com.alten.ecommerce.models.dtos;

import com.alten.ecommerce.models.InventoryStatus;
import com.alten.ecommerce.models.Product;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record ProductDTO(
        Long id,
        @NotBlank @Size(min = 1, max = 50) String code,
        @NotBlank @Size(min = 1, max = 100) String name,
        @Size(max = 500) String description,
        String image,
        @NotBlank @Size(min = 1, max = 50) String category,
        BigDecimal price,
        @Min(value = 0) Integer quantity,
        String internalReference,
        Long shellId,
        InventoryStatus inventoryStatus,
        BigDecimal rating,
        Long createdAt,
        Long updatedAt
) {
    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getImage(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getPrice(),
                product.getQuantity(),
                product.getInternalReference(),
                product.getShellId(),
                product.getInventoryStatus(),
                product.getRating(),
                product.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                product.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }
}
