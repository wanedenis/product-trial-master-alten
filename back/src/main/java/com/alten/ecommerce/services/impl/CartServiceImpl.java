package com.alten.ecommerce.services.impl;

import com.alten.ecommerce.models.Cart;
import com.alten.ecommerce.models.CartItem;
import com.alten.ecommerce.models.Product;
import com.alten.ecommerce.models.User;
import com.alten.ecommerce.models.dtos.CartDTO;
import com.alten.ecommerce.models.dtos.CartItemDTO;
import com.alten.ecommerce.repositories.CartRepository;
import com.alten.ecommerce.repositories.ProductRepository;
import com.alten.ecommerce.repositories.UserRepository;
import com.alten.ecommerce.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CartDTO addItemToCart(String email, CartItemDTO cartItemDTO) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));

        Product product = productRepository.findById(cartItemDTO.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + cartItemDTO.productId()));

        if (product.getQuantity() < cartItemDTO.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.getName());
        }

        Cart cart = cartRepository.findByUser_Email(email)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(cartItemDTO.productId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + cartItemDTO.quantity());
        cartRepository.save(cart);

        return new CartDTO(cart.getId(), cart.getItems().stream()
                .map(i -> new CartItemDTO(i.getProduct().getId(), i.getQuantity()))
                .toList());
    }

    @Override
    public CartDTO getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + email));

        Cart cart = cartRepository.findByUser_Email(email)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        return new CartDTO(cart.getId(), cart.getItems().stream()
                .map(i -> new CartItemDTO(i.getProduct().getId(), i.getQuantity()))
                .toList());
    }
}
