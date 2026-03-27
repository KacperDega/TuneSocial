package com.tunesocial.backend.user.exception;

public class ProfileAlreadySetupException extends RuntimeException {
    public ProfileAlreadySetupException(Long userId) {
        super("Profile has already been set up for user ID: " + userId);
    }
}
