package com.alten.ecommerce.services.impl;

import com.alten.ecommerce.models.User;
import com.alten.ecommerce.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImplUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public ImplUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List <String> roles = new ArrayList<>();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }
}