package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //TODO: GOOD FOR NOW, MIGHT LACK DETAILS IN FUTURE
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Request validation failed"
        );
    }
}
