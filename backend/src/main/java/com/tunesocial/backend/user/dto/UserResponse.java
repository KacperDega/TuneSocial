package com.tunesocial.backend.user.dto;

public record UserResponse(
        Long id,
        String email,
        String username
) {
}
