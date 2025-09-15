package com.alten.ecommerce.repositories;

import com.alten.ecommerce.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user id
     * @param userId the user id
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByUser_Email(String userId);

}
