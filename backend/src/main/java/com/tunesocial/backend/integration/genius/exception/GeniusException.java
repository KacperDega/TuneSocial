package com.tunesocial.backend.integration.genius.exception;

public abstract class GeniusException extends RuntimeException {
    public GeniusException(String message) {
        super(message);
    }

    public GeniusException(String message, Throwable cause) {
        super(message, cause);
    }
}
