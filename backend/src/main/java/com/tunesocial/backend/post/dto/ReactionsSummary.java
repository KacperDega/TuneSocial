package com.tunesocial.backend.post.dto;

import com.tunesocial.backend.post.model.enums.ReactionType;

import java.util.Map;

public record ReactionsSummary(
        long totalCount,
        Map<ReactionType, Long> countsByType,
        ReactionType myReaction
) {}
