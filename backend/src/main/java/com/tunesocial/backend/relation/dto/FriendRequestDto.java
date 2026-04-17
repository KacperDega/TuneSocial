package com.tunesocial.backend.relation.dto;

import com.tunesocial.backend.user.dto.UserRefDto;

import java.time.Instant;

public record FriendRequestDto(
        long id,
        UserRefDto requester,
        long recipientId,
        Instant createdAt
) {}
