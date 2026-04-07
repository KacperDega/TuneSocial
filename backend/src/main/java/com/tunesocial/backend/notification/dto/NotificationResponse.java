package com.tunesocial.backend.notification.dto;

import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        NotificationTargetType targetType,
        String targetId,
        NotificationContext context,
        boolean isRead,
        Instant createdAt
) {}
