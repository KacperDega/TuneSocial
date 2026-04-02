package com.tunesocial.backend.social.exception;

public class InvalidParentCommentException extends RuntimeException {
    public InvalidParentCommentException(String message) {
        super(message);
    }
}
