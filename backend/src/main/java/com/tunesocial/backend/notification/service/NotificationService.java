package com.tunesocial.backend.notification.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.notification.dto.NotificationContext;
import com.tunesocial.backend.notification.dto.NotificationResponse;
import com.tunesocial.backend.notification.model.Notification;
import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;
import com.tunesocial.backend.notification.repository.NotificationRepository;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
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
            String targetId
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
                notificationRepository.save(notification);
                return;
            }
        }

        Notification notification = new Notification();
        notification.setUserId(recipientUserId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now());

        notificationRepository.save(notification);


        // TODO: CLEANING OLD NOTIFICATIONS
    }

    private boolean shouldRecycle(NotificationType type) {
        return switch (type) {
            case REACTION_POST, REACTION_COMMENT, NEW_FOLLOWER -> true;
            default -> false;
        };
    }


    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Notification> notifications = page.getContent();

        Set<Long> actorIds = notifications.stream()
                .map(Notification::getActorId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, UserRefDto> userRefs = userService.getUserReferencesByIds(actorIds);

        List<NotificationResponse> responses = notifications.stream().map(n -> {
            NotificationContext context = buildContext(n, userRefs);

            return new NotificationResponse(
                    n.getId(),
                    n.getType(),
                    n.getTargetType(),
                    n.getTargetId(),
                    context,
                    n.isRead(),
                    n.getCreatedAt()
            );
        }).toList();

        return new PagedResponse<>(responses, page.hasNext() ? page.getNumber() + 1 : null);
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
                .orElseThrow(() -> new RuntimeException("Notification not found")); //  TODO: EXCEPTION

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot access this notification"); //  TODO: EXCEPTION
        }

        notification.setRead(true);
    }

    private NotificationContext buildContext(Notification n, Map<Long, UserRefDto> userRefs) {
        if (n.getType() == NotificationType.SYSTEM_ANNOUNCEMENT || n.getType() == NotificationType.OTHER) {
            return NotificationContext.forSystem(
                    "System Announcement",
                    null,
                    null,
                    null
            );
        }

        UserRefDto actor = userRefs.getOrDefault(n.getActorId(),
                new UserRefDto(n.getActorId(), null, "User_"+n.getActorId(), null));

        if (isUserToUserNotification(n.getType())) {
            return NotificationContext.forUser(
                    actor,
                    null
            );
        }

        return NotificationContext.forSocial(
                actor,
                null,
                null,
                null
        );
    }

    private boolean isUserToUserNotification(NotificationType type) {
        return switch (type) {
            case NEW_FOLLOWER, FRIEND_REQUEST, FRIEND_ACCEPT -> true;
            default -> false;
        };
    }
}
