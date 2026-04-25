package com.tunesocial.backend.notification.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.notification.dto.NotificationResponse;
import com.tunesocial.backend.notification.model.Notification;
import com.tunesocial.backend.notification.model.NotificationContextData;
import com.tunesocial.backend.notification.exception.NotificationAccessDeniedException;
import com.tunesocial.backend.notification.exception.NotificationNotFoundException;
import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;
import com.tunesocial.backend.notification.repository.NotificationRepository;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    @Nested
    class SendNotification {

        @Test
        @DisplayName("Should skip sending notification when recipient is the actor")
        void shouldDoNothing_whenRecipientIsActor() {
            // Given
            Long userId = 1L;

            // When
            notificationService.sendNotification(
                    userId, userId, NotificationType.FRIEND_REQUEST,
                    NotificationTargetType.USER_PROFILE, "1", null, null, null
            );

            // Then
            verifyNoInteractions(notificationRepository);
        }

        @Test
        @DisplayName("Should create new notification and trigger trimming of old notifications")
        void shouldCreateNewNotification_whenTypeIsNotRecyclable() {
            // Given
            Long recipientId = 1L;
            Long actorId = 2L;
            NotificationType type = NotificationType.FRIEND_REQUEST;
            NotificationTargetType targetType = NotificationTargetType.USER_PROFILE;
            String targetId = "2";

            when(notificationRepository.findOldNotificationIdsToTrim(eq(recipientId), anyInt()))
                    .thenReturn(List.of());

            // When
            notificationService.sendNotification(
                    recipientId, actorId, type, targetType,
                    targetId, "imageUrl.com", "sent request", null
            );

            // Then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());

            Notification saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(recipientId);
            assertThat(saved.getActorId()).isEqualTo(actorId);
            assertThat(saved.getType()).isEqualTo(type);
            assertThat(saved.isRead()).isFalse();
            assertThat(saved.getContext()).isNotNull();
            assertThat(saved.getContext().getTextSnippet()).isEqualTo("sent request");

            verify(notificationRepository).findOldNotificationIdsToTrim(eq(recipientId), anyInt());
        }

        @Test
        @DisplayName("Should recycle existing notification when same action occurs again")
        void shouldRecycleNotification_whenNotificationExistsAndIsRecyclable() {
            // Given
            Long recipientId = 1L;
            Long oldActorId = 2L;
            Long newActorId = 3L;
            NotificationType type = NotificationType.REACTION_POST;
            NotificationTargetType targetType = NotificationTargetType.POST;
            String targetId = "100";

            Notification existing = new Notification();
            existing.setId(50L);
            existing.setUserId(recipientId);
            existing.setActorId(oldActorId);
            existing.setType(type);
            existing.setTargetType(targetType);
            existing.setTargetId(targetId);
            existing.setRead(true);

            NotificationContextData context = new NotificationContextData();
            context.setTextSnippet("Old reaction");
            existing.setContext(context);

            when(notificationRepository.findByUserIdAndTargetTypeAndTargetIdAndType(recipientId, targetType, targetId, type))
                    .thenReturn(Optional.of(existing));

            // When
            notificationService.sendNotification(
                    recipientId, newActorId, type, targetType,
                    targetId, "newImageUrl.com", "New reaction", null
            );

            // Then
            verify(notificationRepository).save(existing);
            assertThat(existing.getActorId()).isEqualTo(newActorId);
            assertThat(existing.isRead()).isFalse();
            assertThat(existing.getContext().getTextSnippet()).isEqualTo("New reaction");
            verify(notificationRepository, never()).findOldNotificationIdsToTrim(any(), anyInt());
        }

        @Test
        @DisplayName("Should remove old notifications and context data when trim condition is met")
        void shouldTrimOldNotifications_whenLimitExceeded() {
            // Given
            Long recipientId = 1L;
            Long actorId = 2L;
            List<Long> idsToDelete = List.of(10L, 11L);

            when(notificationRepository.findOldNotificationIdsToTrim(eq(recipientId), anyInt()))
                    .thenReturn(idsToDelete);

            // When
            notificationService.sendNotification(
                    recipientId, actorId, NotificationType.SYSTEM_ANNOUNCEMENT,
                    NotificationTargetType.SYSTEM, "0", null, null, null
            );

            // Then
            verify(notificationRepository).deleteContextsByNotificationIds(idsToDelete);
            verify(notificationRepository).deleteNotificationsByIds(idsToDelete);
        }
    }

    @Nested
    class GetUserNotifications {

        @Test
        @DisplayName("Should return empty paged response when user has no notifications")
        void shouldReturnEmptyPage_whenNoNotificationsFound() {
            // Given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(notificationRepository.findAllByUserIdWithContextOrderByCreatedAtDesc(userId, pageable))
                    .thenReturn(emptyPage);

            // When
            PagedResponse<NotificationResponse> result = notificationService.getUserNotifications(userId, pageable);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.nextPage()).isNull();
        }

        @Test
        @DisplayName("Should map notifications with actor details and context")
        void shouldMapNotifications_whenNotificationsExist() {
            // Given
            Long userId = 1L;
            Long actorId = 2L;
            Pageable pageable = PageRequest.of(0, 10);

            Notification notification = new Notification();
            notification.setId(100L);
            notification.setUserId(userId);
            notification.setActorId(actorId);
            notification.setType(NotificationType.NEW_FOLLOWER);
            notification.setTargetType(NotificationTargetType.USER_PROFILE);
            notification.setTargetId("2");
            notification.setRead(false);
            notification.setCreatedAt(Instant.now());

            NotificationContextData context = new NotificationContextData();
            context.setImageUrl("avatarUrl.com");
            context.setTextSnippet("started following you");
            context.setActionUrl("followerProfileUrl.com");
            notification.setContext(context);

            Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);
            UserRefDto actorDto = new UserRefDto(actorId, "actorUsername", "actorDisplayName", 1);

            when(notificationRepository.findAllByUserIdWithContextOrderByCreatedAtDesc(userId, pageable))
                    .thenReturn(page);
            when(userService.getUserReferencesByIds(Set.of(actorId)))
                    .thenReturn(Map.of(actorId, actorDto));

            // When
            PagedResponse<NotificationResponse> result = notificationService.getUserNotifications(userId, pageable);

            // Then
            assertThat(result.content()).hasSize(1);
            NotificationResponse response = result.content().get(0);

            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.type()).isEqualTo(NotificationType.NEW_FOLLOWER);
            assertThat(response.targetType()).isEqualTo(NotificationTargetType.USER_PROFILE);
            assertThat(response.targetId()).isEqualTo("2");
            assertThat(response.isRead()).isFalse();

            assertThat(response.context()).isNotNull();
            assertThat(response.context().actorId()).isEqualTo(actorId);
            assertThat(response.context().actorUsername()).isEqualTo("actorUsername");
            assertThat(response.context().actorAvatarId()).isEqualTo(1);

            assertThat(response.context().imageUrl()).isEqualTo("avatarUrl.com");
            assertThat(response.context().textSnippet()).isEqualTo("started following you");
            assertThat(response.context().actionUrl()).isEqualTo("followerProfileUrl.com");
        }
    }

    @Nested
    class MarkAsRead {

        @Test
        @DisplayName("Should throw NotificationNotFoundException when notification does not exist")
        void shouldThrowException_whenNotificationNotFound() {
            // Given
            Long notificationId = 99L;
            Long userId = 1L;

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, userId))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw NotificationAccessDeniedException when user is not the recipient")
        void shouldThrowException_whenUserIsNotOwner() {
            // Given
            Long notificationId = 100L;
            Long ownerId = 1L;
            Long otherUserId = 2L;

            Notification notification = new Notification();
            notification.setId(notificationId);
            notification.setUserId(ownerId);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            // When / Then
            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, otherUserId))
                    .isInstanceOf(NotificationAccessDeniedException.class);
        }

        @Test
        @DisplayName("Should mark notification as read when valid request is provided")
        void shouldMarkAsRead_whenRequestIsValid() {
            // Given
            Long notificationId = 100L;
            Long userId = 1L;

            Notification notification = new Notification();
            notification.setId(notificationId);
            notification.setUserId(userId);
            notification.setRead(false);

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            // When
            notificationService.markAsRead(notificationId, userId);

            // Then
            assertThat(notification.isRead()).isTrue();
        }
    }
}
