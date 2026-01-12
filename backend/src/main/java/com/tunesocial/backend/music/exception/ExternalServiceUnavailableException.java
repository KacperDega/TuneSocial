package com.tunesocial.backend.music.exception;

public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String service) {
        super(service + " is unavailable");
    }

    public ExternalServiceUnavailableException(String service, Throwable cause) {
        super(service + " is unavailable", cause);
    }
}
