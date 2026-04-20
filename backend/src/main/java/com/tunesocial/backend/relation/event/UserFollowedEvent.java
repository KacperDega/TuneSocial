package com.tunesocial.backend.relation.event;

public record UserFollowedEvent(
        Long followerId,
        Long followingId
) {}