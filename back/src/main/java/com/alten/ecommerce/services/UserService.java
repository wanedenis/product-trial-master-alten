package com.alten.ecommerce.services;

import com.alten.ecommerce.models.User;
import com.alten.ecommerce.models.dtos.UserDTO;


public interface UserService {

    User createUser(UserDTO userDTO);

}