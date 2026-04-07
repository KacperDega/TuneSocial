package com.tunesocial.backend.notification.dto;

import com.tunesocial.backend.user.dto.UserRefDto;

public record NotificationContext(
        Long actorId,
        String actorUsername,
        String actorDisplayName,
        Integer actorAvatarId,

        String title,
        String imageUrl,
        String textSnippet,

        String actionUrl
) {
    public static NotificationContext forUser(UserRefDto user, String textSnippet) {
        return new NotificationContext(
                user.userId(),
                user.username(),
                user.displayName(),
                user.avatarId(),
                null, null, textSnippet, null
        );
    }

    public static NotificationContext forSocial(
            UserRefDto actor,
            String targetTitle,
            String targetImageUrl,
            String textSnippet
    ) {
        return new NotificationContext(
                actor.userId(),
                actor.username(),
                actor.displayName(),
                actor.avatarId(),
                targetTitle,
                targetImageUrl,
                textSnippet,
                null
        );
    }

    public static NotificationContext forSystem(String title, String textSnippet, String imageUrl, String actionUrl) {
        return new NotificationContext(
                0L, "system", "System", null,
                title, imageUrl, textSnippet, actionUrl
        );
    }
}
