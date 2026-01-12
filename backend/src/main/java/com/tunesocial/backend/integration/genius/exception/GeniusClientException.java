package com.tunesocial.backend.integration.genius.exception;

public class GeniusClientException extends GeniusException {
    public GeniusClientException(int status) {
        super("Genius client error: " + status);
    }

    public GeniusClientException(int status, Throwable cause) {
        super("Genius client error: " + status, cause);
    }
}
