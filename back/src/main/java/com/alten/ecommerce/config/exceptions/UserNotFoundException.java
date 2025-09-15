package com.alten.ecommerce.config.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }

}
