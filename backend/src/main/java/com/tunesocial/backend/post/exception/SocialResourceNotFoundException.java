package com.tunesocial.backend.post.exception;

public class SocialResourceNotFoundException extends RuntimeException {
    public SocialResourceNotFoundException(String message) {
        super(message);
    }
}
