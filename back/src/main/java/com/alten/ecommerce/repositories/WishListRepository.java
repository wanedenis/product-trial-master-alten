package com.alten.ecommerce.repositories;


import com.alten.ecommerce.models.WishList;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    Optional<WishList> findByUser_Email(@NotBlank(message = "Email is mandatory") @Email(message = "Email should be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String userEmail);
}
