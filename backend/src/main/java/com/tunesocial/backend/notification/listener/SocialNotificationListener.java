package com.tunesocial.backend.notification.listener;

import com.tunesocial.backend.notification.facade.NotificationFacade;
import com.tunesocial.backend.post.event.ReactionAddedEvent;
import com.tunesocial.backend.post.exception.SocialResourceNotFoundException;
import com.tunesocial.backend.post.model.FeedItem;
import com.tunesocial.backend.post.model.PostComment;
import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.repository.FeedItemRepository;
import com.tunesocial.backend.post.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialNotificationListener {

    private final NotificationFacade notificationFacade;
    private final FeedItemRepository feedItemRepository;
    private final PostCommentRepository commentRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReactionAdded(ReactionAddedEvent event) {
        try {
            if (event.targetType() == ReactionTargetType.POST) {
                Long postId = Long.parseLong(event.targetId());
                FeedItem post = feedItemRepository.findById(postId)
                        .orElseThrow(() -> new SocialResourceNotFoundException("Post not found for notification, postId: " + postId));

                String snippet = post.getContent();

                notificationFacade.notifyOnReaction(
                        post.getUserId(),
                        event.actorId(),
                        ReactionTargetType.POST,
                        event.targetId(),
                        post.getContext().getImageUrl(),
                        snippet
                );
            } else if (event.targetType() == ReactionTargetType.COMMENT) {
                Long commentId = Long.parseLong(event.targetId());
                PostComment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new SocialResourceNotFoundException("Comment not found for notification,  commentId: " + commentId));

                FeedItem post = feedItemRepository.findById(comment.getPostId())
                        .orElseThrow(() -> new SocialResourceNotFoundException("Post not found for notification,  postId: " + commentId));

                notificationFacade.notifyOnReaction(
                        comment.getUserId(),
                        event.actorId(),
                        ReactionTargetType.COMMENT,
                        event.targetId(),
                        post.getContext().getImageUrl(),
                        comment.getContent()
                );
            }
        } catch (Exception e) {
            log.error("Failed to process reaction notification for: {}(ID: {})", event.targetType(), event.targetId(), e);
        }
    }
}
