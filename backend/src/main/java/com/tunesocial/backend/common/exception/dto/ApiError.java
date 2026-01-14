package com.tunesocial.backend.common.exception.dto;

public record ApiError(
        int status,
        String code,
        String message
) {
}
