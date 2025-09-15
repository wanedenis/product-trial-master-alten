package com.alten.ecommerce.models.dtos;

import jakarta.validation.constraints.NotNull;


public record WishListItemDTO(
        @NotNull Long productId
) {}
