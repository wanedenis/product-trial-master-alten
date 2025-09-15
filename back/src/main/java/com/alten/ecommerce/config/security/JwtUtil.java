package com.alten.ecommerce.config.security;


import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    // Secret key for signing JWTs, loaded from application.properties
    @Value("${spring.security.jwt.secret}")
    private String secret;

    // Token validity period (e.g., 5 hours in milliseconds)
    @Value("${spring.security.jwt.expiration:18000000}")
    private long expiration;

    /**
     * Generates a JWT token for the given user.
     *
     * @param userDetails User details containing username and authorities
     * @return JWT token as a String
     */
    public String generateToken(UserDetails userDetails) {
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername()) // Email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret) // Sign with secret
                .compact();
        System.out.println("Generated token for subject: " + userDetails.getUsername() + " with secret: " + secret); // Debug
        return token;
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Validates a JWT token against the provided user details.
     *
     * @param token       The JWT token to validate
     * @param userDetails User details to validate against
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String email = getEmailFromToken(token);
            boolean isValid = email.equals(userDetails.getUsername()) && !isTokenExpired(token);
            System.out.println("Token validation for email: " + email + ", valid: " + isValid); // Debug
            return isValid;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage()); // Debug
            return false;
        }
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token The JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // Expired or invalid
        }
    }

    public static String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // or getEmail() if you have it
        }

        return principal.toString(); // fallback (might be just a String)
    }
}