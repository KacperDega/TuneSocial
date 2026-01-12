package com.tunesocial.backend.integration.genius.exception;

public class GeniusNotFoundException extends GeniusException {
    public GeniusNotFoundException(String id) {
        super("Genius item not found with id: " + id);
    }

    public GeniusNotFoundException(String id, Throwable cause) {
        super("Genius item not found with id: " + id, cause);
    }
}
