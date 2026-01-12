package com.tunesocial.backend.integration.genius.exception;

public class GeniusServerException extends GeniusException {
    public GeniusServerException() {
        super("Genius API unavailable");
    }

    public GeniusServerException(Throwable cause) {
        super("Genius API unavailable", cause);
    }
}
