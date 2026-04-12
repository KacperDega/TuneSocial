package com.tunesocial.backend.social.event;

public record CommentCreatedEvent(
        Long commentId,
        Long postId,
        Long actorId,
        Long parentCommentId,
        String commentContent
) {}
