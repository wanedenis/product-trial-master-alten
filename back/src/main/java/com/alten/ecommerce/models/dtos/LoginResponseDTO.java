package com.alten.ecommerce.models.dtos;

import org.springframework.security.core.userdetails.UserDetails;

public record LoginResponseDTO(
        String token,
        String email
) {
    public static LoginRequestDTO fromEntity(UserDetails user, String token) {
        return new LoginRequestDTO(
                token,
                user.getUsername()
        );
    }
}
