package com.alten.ecommerce.services;

import com.alten.ecommerce.models.dtos.WishListDTO;
import com.alten.ecommerce.models.dtos.WishListItemDTO;

public interface WishListService {

    WishListDTO addItemToWishList(String email, WishListItemDTO itemDTO);

    WishListDTO getWishList(String email);

    WishListDTO removeItemFromWishList(String email, Long itemId);

}
