package com.tunesocial.backend.relation.event;

public record FriendRequestSentEvent(
        Long requesterId,
        Long recipientId
) {}
