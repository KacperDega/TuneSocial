package com.tunesocial.backend.social.dto;

import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.model.enums.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(
        @NotNull Long targetId,
        @NotNull ReactionTargetType targetType,
        @NotNull ReactionType reactionType
) {}
