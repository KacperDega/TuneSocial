package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.notification.exception.NotificationAccessDeniedException;
import com.tunesocial.backend.notification.exception.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotificationNotFound(NotificationNotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "NOTIFICATION_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NotificationAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleNotificationAccessDenied(NotificationAccessDeniedException ex) {
        return new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "NOTIFICATION_ACCESS_DENIED",
                ex.getMessage()
        );
    }
}
