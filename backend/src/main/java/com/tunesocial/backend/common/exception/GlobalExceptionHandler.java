package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // jakarta validation
    //TODO: GOOD FOR NOW, MIGHT LACK DETAILS IN FUTURE
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex) {
        return new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiError handleIllegalState(IllegalStateException ex) {
        return new ApiError(
                401,
                "USER_NOT_AUTHENTICATED",
                ex.getMessage()
        );
    }
}
