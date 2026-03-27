package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.user.exception.ProfileAlreadySetupException;
import com.tunesocial.backend.user.exception.UserNotFoundException;
import com.tunesocial.backend.user.exception.UserProfileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFound(UserNotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserProfileNotFound(UserProfileNotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "USER_PROFILE_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ProfileAlreadySetupException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleProfileAlreadySetup(ProfileAlreadySetupException ex) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                "PROFILE_ALREADY_SETUP",
                ex.getMessage()
        );
    }
}
