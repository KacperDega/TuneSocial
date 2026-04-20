package com.tunesocial.backend.relation.event;

public record FriendRequestAcceptedEvent(
        Long accepterId,
        Long requesterId
) {}