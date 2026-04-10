package com.tunesocial.backend.notification.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.notification.dto.NotificationContext;
import com.tunesocial.backend.notification.dto.NotificationResponse;
import com.tunesocial.backend.notification.exception.NotificationAccessDeniedException;
import com.tunesocial.backend.notification.exception.NotificationNotFoundException;
import com.tunesocial.backend.notification.model.Notification;
import com.tunesocial.backend.notification.model.NotificationContextData;
import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;
import com.tunesocial.backend.notification.repository.NotificationRepository;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Value("${app.cache.ttl-days:100}")
    private int MAX_NOTIFICATIONS_PER_USER;

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional
    public void sendNotification(
            Long recipientUserId,
            Long actorId,
            NotificationType type,
            NotificationTargetType targetType,
            // context fields
            String targetId,
            String title,
            String imageUrl,
            String textSnippet,
            String actionUrl
    ) {
        if (recipientUserId.equals(actorId)) {
            return;
        }

        if (shouldRecycle(type)) {
            Optional<Notification> existingNotification = notificationRepository
                    .findByUserIdAndTargetTypeAndTargetIdAndType(recipientUserId, targetType, targetId, type);

            if (existingNotification.isPresent()) {
                Notification notification = existingNotification.get();
                notification.setActorId(actorId);
                notification.setRead(false);
                notification.setCreatedAt(Instant.now());

                if (notification.getContext() != null) {
                    notification.getContext().setTextSnippet(textSnippet);
                    notification.getContext().setImageUrl(imageUrl);
                }

                notificationRepository.save(notification);
                return;
            }
        }

        // new notification
        Notification notification = new Notification();
        notification.setUserId(recipientUserId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setRead(false);

        // new context
        NotificationContextData contextData = new NotificationContextData();
        contextData.setNotification(notification);
        contextData.setTitle(title);
        contextData.setImageUrl(imageUrl);
        contextData.setTextSnippet(textSnippet);
        contextData.setActionUrl(actionUrl);

        notification.setContext(contextData);

        notificationRepository.save(notification);

        trimOldNotifications(recipientUserId);
    }

    private boolean shouldRecycle(NotificationType type) {
        return switch (type) {
            case REACTION_POST, REACTION_COMMENT, NEW_FOLLOWER -> true;
            default -> false;
        };
    }

    private void trimOldNotifications(Long userId) {
        List<Long> idsToDelete = notificationRepository.findOldNotificationIdsToTrim(userId, MAX_NOTIFICATIONS_PER_USER);

        if (!idsToDelete.isEmpty()) {
            notificationRepository.deleteContextsByNotificationIds(idsToDelete);

            notificationRepository.deleteNotificationsByIds(idsToDelete);

            log.debug("Removed {} old notifications for userId: {}", idsToDelete.size(), userId);
        }
    }


    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findAllByUserIdWithContextOrderByCreatedAtDesc(userId, pageable);
        List<Notification> notifications = page.getContent();

        Set<Long> actorIds = notifications.stream()
                .map(Notification::getActorId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, UserRefDto> userRefs = userService.getUserReferencesByIds(actorIds);

        List<NotificationResponse> responses = notifications.stream().map(n -> {
            UserRefDto actor = userRefs.getOrDefault(n.getActorId(),
                    new UserRefDto(n.getActorId(), null, "User_" + n.getActorId(), 1));

            NotificationContext contextDto = getNotificationContext(n, actor);

            return new NotificationResponse(
                    n.getId(),
                    n.getType(),
                    n.getTargetType(),
                    n.getTargetId(),
                    contextDto,
                    n.isRead(),
                    n.getCreatedAt()
            );
        }).toList();

        return new PagedResponse<>(responses, page.hasNext() ? page.getNumber() + 1 : null);
    }

    private static NotificationContext getNotificationContext(Notification n, UserRefDto actor) {
        NotificationContextData context = n.getContext();

        return new NotificationContext(
                actor.userId(),
                actor.username(),
                actor.displayName(),
                actor.avatarId(),
                context != null ? context.getTitle() : null,
                context != null ? context.getImageUrl() : null,
                context != null ? context.getTextSnippet() : null,
                context != null ? context.getActionUrl() : null
        );
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException(notificationId);
        }

        notification.setRead(true);
    }
}
