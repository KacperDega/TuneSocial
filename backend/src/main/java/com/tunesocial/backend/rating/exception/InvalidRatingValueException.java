package com.tunesocial.backend.rating.exception;

public class InvalidRatingValueException extends RuntimeException
{
    public InvalidRatingValueException(int value) {
        super("Invalid value: "+ value +". Rating value must be between 1 and 10.");
    }
}
