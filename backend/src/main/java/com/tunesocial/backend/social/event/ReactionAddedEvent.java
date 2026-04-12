package com.tunesocial.backend.social.event;

import com.tunesocial.backend.social.model.enums.ReactionTargetType;

public record ReactionAddedEvent(
        Long actorId,
        ReactionTargetType targetType,
        String targetId
) {}
