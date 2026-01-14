package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.rating.exception.InvalidRatingValueException;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.exception.RatingSummaryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RatingExceptionHandler {

    @ExceptionHandler(InvalidRatingValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRatingValue(InvalidRatingValueException e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_RATING_VALUE",
                "Rating value must be between 1 and 10."
        );
    }

    @ExceptionHandler(RatingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRatingNotFound(RatingNotFoundException e) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "RATING_NOT_FOUND",
                "Requested rating was not found."
        );
    }

    @ExceptionHandler(RatingSummaryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleSummaryBroken(RatingSummaryNotFoundException e) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "RATING_SUMMARY_NOT_FOUND",
                "Requested rating summary was not found."
        );
    }
}
