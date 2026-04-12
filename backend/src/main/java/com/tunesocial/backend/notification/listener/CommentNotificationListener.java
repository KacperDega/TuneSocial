package com.tunesocial.backend.notification.listener;

import com.tunesocial.backend.notification.facade.NotificationFacade;
import com.tunesocial.backend.post.event.CommentCreatedEvent;
import com.tunesocial.backend.post.exception.SocialResourceNotFoundException;
import com.tunesocial.backend.post.model.FeedItem;
import com.tunesocial.backend.post.repository.FeedItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentNotificationListener {

    private final NotificationFacade notificationFacade;
    private final FeedItemRepository feedItemRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            FeedItem post = feedItemRepository.findById(event.postId())
                    .orElseThrow(() -> new SocialResourceNotFoundException("Post ("+ event.postId() + ") not found for notification"));

            if (event.parentCommentId() != null) {

                notificationFacade.notifyOnCommentReply(
                        event.parentCommentId(),
                        event.actorId(),
                        event.commentId(),
                        event.commentContent()
                );
            } else {
                notificationFacade.notifyOnComment(
                        post.getUserId(),
                        event.actorId(),
                        post.getId(),
                        event.commentContent()
                );
            }
        } catch (Exception e) {
            log.error("Failed to process comment notification for commentId: {}", event.commentId(), e);
        }
    }
}
