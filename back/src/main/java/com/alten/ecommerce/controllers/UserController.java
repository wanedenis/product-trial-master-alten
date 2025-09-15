package com.alten.ecommerce.controllers;

import com.alten.ecommerce.config.exceptions.UserNotFoundException;
import com.alten.ecommerce.config.security.JwtUtil;
import com.alten.ecommerce.models.dtos.ApiResponseDTO;
import com.alten.ecommerce.models.dtos.LoginRequestDTO;
import com.alten.ecommerce.models.dtos.LoginResponseDTO;
import com.alten.ecommerce.models.dtos.UserDTO;
import com.alten.ecommerce.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Authentication API", description = "Endpoints for user account management and authentication")
@RestController
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public UserController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserDetailsService userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/account")
    @Operation(summary = "Create a new user account", description = "Registers a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDTO> createAccount(@Valid @RequestBody UserDTO userDTO) {
        userService.createUser(userDTO);
        ApiResponseDTO response = new ApiResponseDTO(HttpStatus.CREATED.value(),"User account created successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token")
    @Operation(summary = "Authenticate user and generate JWT token", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponseDTO> generateToken(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new LoginResponseDTO(token, userDetails.getUsername()));
        } catch (Exception e) {
            throw new UserNotFoundException("Invalid email or password");
        }
    }
}
