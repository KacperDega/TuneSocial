package com.tunesocial.backend.post.event;

public record CommentCreatedEvent(
        Long commentId,
        Long postId,
        Long actorId,
        Long parentCommentId,
        String commentContent
) {}
