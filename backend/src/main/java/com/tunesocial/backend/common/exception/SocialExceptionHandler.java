package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.post.exception.InvalidParentCommentException;
import com.tunesocial.backend.post.exception.SocialResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SocialExceptionHandler {

    @ExceptionHandler(SocialResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleSocialResourceNotFound(SocialResourceNotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "SOCIAL_RESOURCE_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidParentCommentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidParentCommentException(InvalidParentCommentException ex) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_PARENT_COMMENT",
                ex.getMessage()
        );
    }
}
