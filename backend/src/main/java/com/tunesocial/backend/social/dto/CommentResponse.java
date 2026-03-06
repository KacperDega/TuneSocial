package com.tunesocial.backend.social.dto;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Long id,
        Long userId,
        String username,
        String content,
        Long parentId,
        ReactionsSummary reactions,
        long repliesCount,
        Instant createdAt,
        List<CommentResponse> replies
) {}
