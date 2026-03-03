package com.tunesocial.backend.social.dto;

import com.tunesocial.backend.social.model.enums.ReactionType;

import java.util.Map;

public record ReactionsSummary(
        long totalCount,
        Map<ReactionType, Long> countsByType,
        ReactionType myReaction
) {}
