package com.tunesocial.backend.social.exception;

public class SocialResourceNotFoundException extends RuntimeException {
    public SocialResourceNotFoundException(String message) {
        super(message);
    }
}
