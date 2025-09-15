package com.alten.ecommerce.services.impl;

import com.alten.ecommerce.models.User;
import com.alten.ecommerce.models.dtos.UserDTO;
import com.alten.ecommerce.repositories.UserRepository;
import com.alten.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + userDTO.email());
        }
        if (userRepository.existsByUsername(userDTO.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + userDTO.username());
        }

        User user = new User();
        user.setUsername(userDTO.username());
        user.setFirstname(userDTO.firstname());
        user.setEmail(userDTO.email());
        user.setPassword(passwordEncoder.encode(userDTO.password())); // Hash password

        return userRepository.save(user);
    }
}
