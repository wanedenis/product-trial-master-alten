package com.alten.ecommerce.models.dtos;


import java.util.List;

public record CartDTO(
        Long id,
        List<CartItemDTO> items
) {}
