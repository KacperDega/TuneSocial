package com.tunesocial.backend.relation.exception;

public class UnauthorizedRelationAccessException extends RuntimeException {
    public UnauthorizedRelationAccessException(String message) {
        super(message);
    }
}
