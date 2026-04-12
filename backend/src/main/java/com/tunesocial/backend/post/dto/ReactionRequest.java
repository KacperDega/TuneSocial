package com.tunesocial.backend.post.dto;

import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.model.enums.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(
        @NotNull Long targetId,
        @NotNull ReactionTargetType targetType,
        @NotNull ReactionType reactionType
) {}
