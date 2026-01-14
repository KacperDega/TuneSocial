package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.exception.RatingSummaryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RatingExceptionHandler {

    @ExceptionHandler(RatingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRatingNotFound(RatingNotFoundException e) {
        return new ApiError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(RatingSummaryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleSummaryBroken(RatingSummaryNotFoundException e) {
        return new ApiError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }
}
