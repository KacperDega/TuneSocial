package com.tunesocial.backend.post.dto;

import com.tunesocial.backend.post.model.enums.FeedItemType;

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
