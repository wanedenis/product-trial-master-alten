package com.alten.ecommerce.services;

import com.alten.ecommerce.models.dtos.CartDTO;
import com.alten.ecommerce.models.dtos.CartItemDTO;

public interface CartService {

    CartDTO getCart(String email);

    CartDTO addItemToCart(String email, CartItemDTO cartItemDTO);
}
