package com.tunesocial.backend.rating.exception;

public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(Long id) {
        super("Rating not found with id: " + id);
    }
}