package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.auth.exception.EmailAlreadyExistsException;
import com.tunesocial.backend.auth.exception.InvalidCredentialsException;
import com.tunesocial.backend.auth.exception.UsernameAlreadyExistsException;
import com.tunesocial.backend.common.exception.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Invalid username or password."
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUsernameExists(UsernameAlreadyExistsException ex) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                "USERNAME_ALREADY_EXISTS",
                "Username is already taken."
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailExists(EmailAlreadyExistsException ex) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                "EMAIL_ALREADY_EXISTS",
                "Email is already registered."
        );
    }

}
