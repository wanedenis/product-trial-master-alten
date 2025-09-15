package com.alten.ecommerce.models.dtos;

import java.util.List;

public record WishListDTO(
        Long id,
        List<Long> productIds
) {}
