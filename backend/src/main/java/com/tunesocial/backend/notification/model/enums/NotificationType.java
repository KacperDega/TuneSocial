package com.tunesocial.backend.notification.model.enums;

public enum NotificationType {
    // social
    COMMENT_ON_POST,
    REPLY_TO_COMMENT,
    REACTION_POST,
    REACTION_COMMENT,

    // friends followers
    NEW_FOLLOWER,
    FRIEND_REQUEST,
    FRIEND_ACCEPT,

    // others
    SYSTEM_ANNOUNCEMENT,
    OTHER
}
