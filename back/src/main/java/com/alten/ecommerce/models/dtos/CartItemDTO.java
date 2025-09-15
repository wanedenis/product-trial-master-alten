package com.alten.ecommerce.models.dtos;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemDTO(
        @NotNull Long productId,
        @Min(1) int quantity
) {}
