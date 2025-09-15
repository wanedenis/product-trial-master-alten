package com.alten.ecommerce.controllers;

import com.alten.ecommerce.config.security.JwtUtil;
import com.alten.ecommerce.models.dtos.CartDTO;
import com.alten.ecommerce.models.dtos.CartItemDTO;
import com.alten.ecommerce.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Authenticated users only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added to cart"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User or product not found")
    })
    public ResponseEntity<CartDTO> addItemToCart(
            @RequestBody CartItemDTO cartItemDTO,
            String email
    ) {
        CartDTO cart = cartService.addItemToCart(email, cartItemDTO);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    @Operation(summary = "Get user's cart", description = "Authenticated users only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Cart or user not found")
    })
    public ResponseEntity<CartDTO> getCart() {
        String currentUserEmail = JwtUtil.getCurrentUserEmail();
        CartDTO cart = cartService.getCart(currentUserEmail);
        return ResponseEntity.ok(cart);
    }
}
