package com.tunesocial.backend.post.event;

import com.tunesocial.backend.post.model.enums.ReactionTargetType;

public record ReactionAddedEvent(
        Long actorId,
        ReactionTargetType targetType,
        String targetId
) {}
