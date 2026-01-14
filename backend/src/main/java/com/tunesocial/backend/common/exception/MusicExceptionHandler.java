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
                "MUSIC_ITEM_NOT_FOUND",
                "Requested music item was not found."
        );
    }

    @ExceptionHandler(ExternalServiceClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiError handleExternalServiceClient(ExternalServiceClientException ex) {
        return new ApiError(
                HttpStatus.BAD_GATEWAY.value(),
                "EXTERNAL_SERVICE_CLIENT_ERROR",
                "Upstream service returned an invalid response."
        );
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleExternalServiceUnavailable(ExternalServiceUnavailableException ex) {
        return new ApiError(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "EXTERNAL_SERVICE_UNAVAILABLE",
                "Upstream service is temporarily unavailable."
        );
    }
}
