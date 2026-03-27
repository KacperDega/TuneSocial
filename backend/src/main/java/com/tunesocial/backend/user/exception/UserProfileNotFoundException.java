package com.tunesocial.backend.user.exception;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String username) {
        super("Profile not found for username: " + username);
    }

    public UserProfileNotFoundException(Long userId) {
        super("Profile not found for userid: " + userId);
    }
}
