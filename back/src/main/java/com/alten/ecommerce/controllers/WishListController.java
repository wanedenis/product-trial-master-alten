package com.alten.ecommerce.controllers;

import com.alten.ecommerce.models.dtos.WishListDTO;
import com.alten.ecommerce.models.dtos.WishListItemDTO;
import com.alten.ecommerce.services.WishListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
public class WishListController {

    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    @PostMapping("/add")
    @Operation(summary = "Add product to wishlist", description = "Authenticated users only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product added to wishlist"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User or product not found"),
            @ApiResponse(responseCode = "409", description = "Product already in wishlist")
    })
    public ResponseEntity<WishListDTO> addItemToWishList(
            @RequestBody WishListItemDTO itemDTO,
            Authentication authentication
    ) {
        String email = authentication.getName(); // Principal (email)
        WishListDTO wishList = wishListService.addItemToWishList(email, itemDTO);
        return ResponseEntity.ok(wishList);
    }

    @GetMapping
    @Operation(summary = "Get user's wishlist", description = "Authenticated users only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wishlist or user not found")
    })
    public ResponseEntity<WishListDTO> getWishList(Authentication authentication) {
        String email = authentication.getName();
        WishListDTO wishList = wishListService.getWishList(email);
        return ResponseEntity.ok(wishList);
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Remove item from wishlist", description = "Authenticated users only")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed from wishlist"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Wishlist item or user not found")
    })
    public ResponseEntity<WishListDTO> removeItemFromWishList(
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        WishListDTO wishList = wishListService.removeItemFromWishList(email, itemId);
        return ResponseEntity.ok(wishList);
    }
}