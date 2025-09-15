package com.alten.ecommerce.services.impl;

import com.alten.ecommerce.models.Product;
import com.alten.ecommerce.models.User;
import com.alten.ecommerce.models.WishList;
import com.alten.ecommerce.models.WishListItem;
import com.alten.ecommerce.models.dtos.WishListDTO;
import com.alten.ecommerce.models.dtos.WishListItemDTO;
import com.alten.ecommerce.repositories.ProductRepository;
import com.alten.ecommerce.repositories.UserRepository;
import com.alten.ecommerce.repositories.WishListRepository;
import com.alten.ecommerce.services.WishListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public WishListServiceImpl(WishListRepository wishListRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.wishListRepository = wishListRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WishListDTO addItemToWishList(String email, WishListItemDTO itemDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Product product = productRepository.findById(itemDTO.productId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.productId()));

        WishList wishList = getWishList(user);

        // Check if item already exists
        if (wishList.getItems().stream().anyMatch(i -> i.getProduct().getId().equals(itemDTO.productId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already in wishlist: " + product.getName());
        }

        WishListItem item = new WishListItem();
        item.setWishList(wishList);
        item.setProduct(product);
        wishList.getItems().add(item);
        wishListRepository.save(wishList);

        return new WishListDTO(wishList.getId(), wishList.getItems().stream()
                .map(i -> i.getProduct().getId())
                .toList());
    }

    private WishList getWishList(User user) {
        return wishListRepository.findByUser_Email(user.getEmail())
                .orElseGet(() -> {
                    WishList newWishList = new WishList();
                    newWishList.setUser(user);
                    return wishListRepository.save(newWishList);
                });
    }

    @Override
    public WishListDTO getWishList(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        WishList wishList = getWishList(user);

        return new WishListDTO(wishList.getId(), wishList.getItems().stream()
                .map(i -> i.getProduct().getId())
                .toList());
    }

    public WishListDTO removeItemFromWishList(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        WishList wishList = getWishList(user);

        WishListItem item = wishList.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Wishlist item not found: " + itemId));

        wishList.getItems().remove(item);
        wishListRepository.save(wishList);

        return new WishListDTO(wishList.getId(), wishList.getItems().stream()
                .map(i -> i.getProduct().getId())
                .toList());
    }
}
