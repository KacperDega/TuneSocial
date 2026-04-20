package com.tunesocial.backend.notification.listener;

import com.tunesocial.backend.relation.event.FriendRequestAcceptedEvent;
import com.tunesocial.backend.relation.event.FriendRequestSentEvent;
import com.tunesocial.backend.relation.event.UserFollowedEvent;
import com.tunesocial.backend.user.exception.UserProfileNotFoundException;
import com.tunesocial.backend.user.model.UserProfile;
import com.tunesocial.backend.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.tunesocial.backend.notification.facade.NotificationFacade;

@Component
@RequiredArgsConstructor
@Slf4j
public class RelationNotificationListener {

    private final NotificationFacade notificationFacade;
    private final UserProfileRepository profileRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserFollowed(UserFollowedEvent event) {
        try {
            UserProfile followerProfile = profileRepository.findById(event.followerId())
                    .orElseThrow(() -> new UserProfileNotFoundException("Follower profile not found with id: " + event.followerId()));

            notificationFacade.notifyOnNewFollower(
                    event.followingId(),
                    event.followerId(),
                    followerProfile.getAvatarId()
            );
        } catch (Exception e) {
            log.error("Failed to process follow notification for followerId: {} and followingId: {}",
                    event.followerId(), event.followingId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestSent(FriendRequestSentEvent event) {
        try {
            UserProfile requesterProfile = profileRepository.findById(event.requesterId())
                    .orElseThrow(() -> new UserProfileNotFoundException("Requester profile not found with id: " + event.requesterId()));

            notificationFacade.notifyOnFriendRequest(
                    event.recipientId(),
                    event.requesterId(),
                    requesterProfile.getAvatarId()
            );
        } catch (Exception e) {
            log.error("Failed to process friend request notification for requesterId: {} and recipientId: {}",
                    event.requesterId(), event.recipientId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        try {
            UserProfile accepterProfile = profileRepository.findById(event.accepterId())
                    .orElseThrow(() -> new UserProfileNotFoundException("Accepter profile not found with id: " + event.accepterId()));

            notificationFacade.notifyOnFriendAccept(
                    event.requesterId(),
                    event.accepterId(),
                    accepterProfile.getAvatarId()
            );
        } catch (Exception e) {
            log.error("Failed to process friend accept notification for accepterId: {} and requesterId: {}",
                    event.accepterId(), event.requesterId(), e);
        }
    }
}
