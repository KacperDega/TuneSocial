package com.tunesocial.backend.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("User not found for username: " + username);
    }

    public UserNotFoundException(Long userId) {
        super("User not found for userid: " + userId);
    }
}
