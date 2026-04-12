package com.tunesocial.backend.notification.facade;

import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;
import com.tunesocial.backend.notification.service.NotificationService;
import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationFacade {

    private final NotificationService notificationService;

    // COMMENTS

    public void notifyOnComment(
            Long postAuthorId,
            Long actorId,
            Long postId,
            String commentTextSnippet
    ) {
        notificationService.sendNotification(
                postAuthorId,
                actorId,
                NotificationType.COMMENT_ON_POST,
                NotificationTargetType.POST,
                postId.toString(),
                null,
                truncSnippet(commentTextSnippet),
                null
        );
    }

    public void notifyOnCommentReply(
            Long parentCommentAuthorId,
            Long actorId,
            Long commentId,
            String replyText
    ) {
        notificationService.sendNotification(
                parentCommentAuthorId,
                actorId,
                NotificationType.REPLY_TO_COMMENT,
                NotificationTargetType.COMMENT,
                commentId.toString(),
                null,
                truncSnippet(replyText),
                null
        );
    }


    // REACTIONS

    public void notifyOnReaction(
            Long targetAuthorId,
            Long actorId,
            ReactionTargetType targetType,
            String targetId,
            String targetImageUrl,
            String textSnippet
    ) {
        NotificationType notificationType = (targetType == ReactionTargetType.POST)
                ? NotificationType.REACTION_POST
                : NotificationType.REACTION_COMMENT;

        NotificationTargetType notificationTarget = (targetType == ReactionTargetType.POST)
                ? NotificationTargetType.POST
                : NotificationTargetType.COMMENT;

        notificationService.sendNotification(
                targetAuthorId,
                actorId,
                notificationType,
                notificationTarget,
                targetId,
                targetImageUrl,
                null,
                null
        );
    }


    // SOCIAL

    public void notifyOnNewFollower(Long followedUserId, Long actorId, Integer followerAvatarId) {
        notificationService.sendNotification(
                followedUserId,
                actorId,
                NotificationType.NEW_FOLLOWER,
                NotificationTargetType.USER_PROFILE,
                actorId.toString(),
                followerAvatarId != null ? followerAvatarId.toString() : null,
                null,
                null
        );
    }

    public void notifyOnFriendRequest(Long recipientUserId, Long actorId, Integer requesterAvatarId) {
        notificationService.sendNotification(
                recipientUserId,
                actorId,
                NotificationType.FRIEND_REQUEST,
                NotificationTargetType.USER_PROFILE,
                actorId.toString(),
                requesterAvatarId != null ? requesterAvatarId.toString() : null,
                null,
                null
        );
    }

    public void notifyOnFriendAccept(Long requesterUserId, Long actorId, Integer accepterAvatarId) {
        notificationService.sendNotification(
                requesterUserId,
                actorId,
                NotificationType.FRIEND_ACCEPT,
                NotificationTargetType.USER_PROFILE,
                actorId.toString(),
                accepterAvatarId != null ? accepterAvatarId.toString() : null,
                null,
                null
        );
    }


    // OTHER

    public void notifySystemAnnouncement(
            Long recipientUserId,
            String imageUrl,
            String textSnippet,
            String actionUrl
    ) {
        notificationService.sendNotification(
                recipientUserId,
                0L,
                NotificationType.SYSTEM_ANNOUNCEMENT,
                NotificationTargetType.SYSTEM,
                null,
                imageUrl,
                truncSnippet(textSnippet),
                actionUrl
        );
    }

    public void notifyOther(
            Long recipientUserId,
            Long actorId,
            NotificationTargetType targetType,
            String targetId,
            String imageUrl,
            String textSnippet,
            String actionUrl
    ) {
        notificationService.sendNotification(
                recipientUserId,
                actorId,
                NotificationType.OTHER,
                targetType,
                targetId,
                imageUrl,
                truncSnippet(textSnippet),
                actionUrl
        );
    }



    private String truncSnippet(String text) {
        if (text == null) return null;
        text = text.trim();
        return text.length() > 80 ? text.substring(0, 77) : text;
    }
}
