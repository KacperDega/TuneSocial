package com.tunesocial.backend.post.exception;

public class InvalidParentCommentException extends RuntimeException {
    public InvalidParentCommentException(String message) {
        super(message);
    }
}
