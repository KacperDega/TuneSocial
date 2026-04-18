package com.tunesocial.backend.common.exception;

import com.tunesocial.backend.common.exception.dto.ApiError;
import com.tunesocial.backend.relation.exception.AlreadyRelatedException;
import com.tunesocial.backend.relation.exception.RelationNotFoundException;
import com.tunesocial.backend.relation.exception.SelfRelationException;
import com.tunesocial.backend.relation.exception.UnauthorizedRelationAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RelationExceptionHandler {

    @ExceptionHandler(RelationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRelationNotFound(RelationNotFoundException ex) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "RELATION_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedRelationAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleUnauthorizedRelationAccess(UnauthorizedRelationAccessException ex) {
        return new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "RELATION_ACCESS_DENIED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(AlreadyRelatedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAlreadyRelated(AlreadyRelatedException ex) {
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                "ALREADY_RELATED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(SelfRelationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleSelfRelation(SelfRelationException ex) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "SELF_RELATION",
                ex.getMessage()
        );
    }
}
