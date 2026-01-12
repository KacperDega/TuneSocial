package com.tunesocial.backend.music.exception;

public class ExternalServiceClientException extends RuntimeException {
    public ExternalServiceClientException(String service) {
        super(service + " returned an invalid client response");
    }

    public ExternalServiceClientException(String service, Throwable cause) {
        super(service + " returned an invalid client response", cause);
    }
}
