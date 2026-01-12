package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.music.exception.ExternalServiceClientException;
import com.tunesocial.backend.music.exception.ExternalServiceUnavailableException;
import com.tunesocial.backend.music.exception.MusicItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MusicExceptionHandler {

    @ExceptionHandler(MusicItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleMusicItemNotFound(MusicItemNotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExternalServiceClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiError handleExternalServiceClient(ExternalServiceClientException ex) {
        return new ApiError(
                HttpStatus.BAD_GATEWAY.value(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleExternalServiceUnavailable(ExternalServiceUnavailableException ex) {
        return new ApiError(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage()
        );
    }
}
