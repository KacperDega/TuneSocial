package com.tunesocial.backend.user.dto;

public record UserResponse(
        Long id,
        String username,
        String email
) {
}
