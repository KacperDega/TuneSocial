package com.tunesocial.backend.social.dto;

import com.tunesocial.backend.social.model.enums.FeedItemType;

import java.time.Instant;

public record FeedItemResponse<T>(
        Long postId,
        FeedItemType type,
        Long userId,
        String username,
        T content,
        long likesCount,
        boolean isLikedByMe,
        Instant createdAt
) {}
