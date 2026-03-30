package com.tunesocial.backend.user.dto;

public record UserRefDto(
        Long userId,
        String username,
        String displayName,
        Integer avatarId
) {}
