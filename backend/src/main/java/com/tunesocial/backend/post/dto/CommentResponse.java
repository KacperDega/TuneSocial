package com.tunesocial.backend.post.dto;

import com.tunesocial.backend.user.dto.UserRefDto;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Long id,
        UserRefDto author,
        String content,
        Long parentId,
        ReactionsSummary reactions,
        long repliesCount,
        Instant createdAt,
        List<CommentResponse> replies
) {}
