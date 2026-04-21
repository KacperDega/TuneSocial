package com.tunesocial.backend.relation.service;

import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.relation.model.FriendRequest;
import com.tunesocial.backend.relation.model.FriendRelation;
import com.tunesocial.backend.relation.event.FriendRequestAcceptedEvent;
import com.tunesocial.backend.relation.event.FriendRequestSentEvent;
import com.tunesocial.backend.relation.exception.AlreadyRelatedException;
import com.tunesocial.backend.relation.exception.RelationNotFoundException;
import com.tunesocial.backend.relation.exception.SelfRelationException;
import com.tunesocial.backend.relation.exception.UnauthorizedRelationAccessException;
import com.tunesocial.backend.relation.repository.FriendRelationRepository;
import com.tunesocial.backend.relation.repository.FriendRequestRepository;
import com.tunesocial.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendRelationRepository friendRelationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FollowService followService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FriendService friendService;

    @Nested
    class SendFriendRequest {

        @Test
        @DisplayName("Should throw SelfRelationException when trying to send friend request to self")
        void shouldThrowSelfRelationException_whenTargetIsSameUser() {
            Long userId = 1L;

            assertThatThrownBy(() -> friendService.sendFriendRequest(userId, userId))
                    .isInstanceOf(SelfRelationException.class);

            verifyNoInteractions(friendRequestRepository, eventPublisher);
        }

        @Test
        @DisplayName("Should throw AlreadyRelatedException when users are already friends")
        void shouldThrowAlreadyRelatedException_whenUsersAreAlreadyFriends() {
            Long requesterId = 1L;
            Long recipientId = 2L;

            when(friendRelationRepository.areFriends(requesterId, recipientId)).thenReturn(true);

            assertThatThrownBy(() -> friendService.sendFriendRequest(requesterId, recipientId))
                    .isInstanceOf(AlreadyRelatedException.class);

            verify(friendRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AlreadyRelatedException when friend request has already been sent")
        void shouldThrowAlreadyRelatedException_whenRequestAlreadyExists() {
            Long requesterId = 1L;
            Long recipientId = 2L;

            when(friendRelationRepository.areFriends(requesterId, recipientId)).thenReturn(false);
            when(friendRequestRepository.existsByRequesterIdAndRecipientId(requesterId, recipientId)).thenReturn(true);

            assertThatThrownBy(() -> friendService.sendFriendRequest(requesterId, recipientId))
                    .isInstanceOf(AlreadyRelatedException.class);

            verify(friendRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should automatically accept request when reverse request already exists")
        void shouldAutoAccept_whenReverseRequestExists() {
            Long requesterId = 1L;
            Long recipientId = 2L;
            Long reverseRequestId = 10L;

            FriendRequest reverseRequest = mock(FriendRequest.class);
            when(reverseRequest.getId()).thenReturn(reverseRequestId);
            when(reverseRequest.getRequesterId()).thenReturn(recipientId);
            when(reverseRequest.getRecipientId()).thenReturn(requesterId);

            when(friendRelationRepository.areFriends(requesterId, recipientId)).thenReturn(false);
            when(friendRequestRepository.existsByRequesterIdAndRecipientId(requesterId, recipientId)).thenReturn(false);
            when(friendRequestRepository.findByRequesterIdAndRecipientId(recipientId, requesterId))
                    .thenReturn(Optional.of(reverseRequest));
            when(friendRequestRepository.findById(reverseRequestId)).thenReturn(Optional.of(reverseRequest));

            friendService.sendFriendRequest(requesterId, recipientId);

            verify(friendRelationRepository).save(any(FriendRelation.class));
            verify(followService).ensureFollow(recipientId, requesterId);
            verify(followService).ensureFollow(requesterId, recipientId);
            verify(friendRequestRepository).delete(reverseRequest);
            verify(eventPublisher).publishEvent(any(FriendRequestAcceptedEvent.class));
            verify(friendRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should save friend request and publish event when valid request is sent")
        void shouldSaveRequestAndPublishEvent_whenValid() {
            Long requesterId = 1L;
            Long recipientId = 2L;

            when(friendRelationRepository.areFriends(requesterId, recipientId)).thenReturn(false);
            when(friendRequestRepository.existsByRequesterIdAndRecipientId(requesterId, recipientId)).thenReturn(false);
            when(friendRequestRepository.findByRequesterIdAndRecipientId(recipientId, requesterId)).thenReturn(Optional.empty());

            friendService.sendFriendRequest(requesterId, recipientId);

            verify(friendRequestRepository).save(any(FriendRequest.class));
            verify(eventPublisher).publishEvent(any(FriendRequestSentEvent.class));
        }
    }

    @Nested
    class AcceptFriendRequest {

        @Test
        @DisplayName("Should throw RelationNotFoundException when request does not exist")
        void shouldThrowNotFound_whenRequestDoesNotExist() {
            Long requestId = 99L;
            Long userId = 1L;

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> friendService.acceptFriendRequest(userId, requestId))
                    .isInstanceOf(RelationNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedRelationAccessException when user is not the recipient")
        void shouldThrowUnauthorized_whenUserIsNotRecipient() {
            Long requestId = 10L;
            Long currentUserId = 1L;
            Long actualRecipientId = 2L;

            FriendRequest request = new FriendRequest(3L, actualRecipientId);
            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> friendService.acceptFriendRequest(currentUserId, requestId))
                    .isInstanceOf(UnauthorizedRelationAccessException.class);
        }

        @Test
        @DisplayName("Should delete request and log warning when self-referencing request is detected")
        void shouldDeleteAndReturn_whenSelfReferencingRequestFound() {
            Long requestId = 10L;
            Long userId = 1L;

            FriendRequest invalidRequest = new FriendRequest(userId, userId);
            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(invalidRequest));

            friendService.acceptFriendRequest(userId, requestId);

            verify(friendRequestRepository).delete(invalidRequest);
            verifyNoInteractions(friendRelationRepository, followService, eventPublisher);
        }

        @Test
        @DisplayName("Should create friendship, setup mutual follows, delete request and publish event")
        void shouldAcceptRequestSuccessfully() {
            Long requestId = 10L;
            Long recipientId = 1L;
            Long requesterId = 2L;

            FriendRequest request = new FriendRequest(requesterId, recipientId);
            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

            friendService.acceptFriendRequest(recipientId, requestId);

            verify(friendRelationRepository).save(any(FriendRelation.class));
            verify(followService).ensureFollow(requesterId, recipientId);
            verify(followService).ensureFollow(recipientId, requesterId);
            verify(friendRequestRepository).delete(request);
            verify(eventPublisher).publishEvent(any(FriendRequestAcceptedEvent.class));
        }
    }

    @Nested
    class CancelOrRejectFriendRequest {

        @Test
        @DisplayName("Should throw RelationNotFoundException when request to cancel does not exist")
        void shouldThrowNotFound_whenRequestDoesNotExist() {
            Long requestId = 99L;
            Long userId = 1L;

            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> friendService.cancelOrRejectFriendRequest(userId, requestId))
                    .isInstanceOf(RelationNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedRelationAccessException when user is neither requester nor recipient")
        void shouldThrowUnauthorized_whenUserIsThirdParty() {
            Long requestId = 10L;
            Long strangerId = 99L;

            FriendRequest request = new FriendRequest(1L, 2L);
            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> friendService.cancelOrRejectFriendRequest(strangerId, requestId))
                    .isInstanceOf(UnauthorizedRelationAccessException.class);

            verify(friendRequestRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should delete request when action performed by recipient")
        void shouldDeleteRequest_whenUserIsRecipient() {
            Long requestId = 10L;
            Long recipientId = 2L;

            FriendRequest request = new FriendRequest(1L, recipientId);
            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

            friendService.cancelOrRejectFriendRequest(recipientId, requestId);

            verify(friendRequestRepository).delete(request);
        }

        @Test
        @DisplayName("Should delete request when action performed by requester")
        void shouldDeleteRequest_whenUserIsRequester() {
            Long requestId = 10L;
            Long requesterId = 1L;

            FriendRequest request = new FriendRequest(requesterId, 2L);
            when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

            friendService.cancelOrRejectFriendRequest(requesterId, requestId);

            verify(friendRequestRepository).delete(request);
        }
    }

    @Nested
    class RemoveFriend {

        @Test
        @DisplayName("Should pass ordered user IDs (asc) when looking up and deleting friendship")
        void shouldSortUserIdsAndRemoveFriendship() {
            Long currentUserId = 5L;
            Long friendId = 2L;

            FriendRelation relation = new FriendRelation(friendId, currentUserId);
            when(friendRelationRepository.findFriendRelationByUserId1AndUserId2(2L, 5L))
                    .thenReturn(Optional.of(relation));

            friendService.removeFriend(currentUserId, friendId);

            verify(friendRelationRepository).findFriendRelationByUserId1AndUserId2(2L, 5L);
            verify(friendRelationRepository).delete(relation);
        }

        @Test
        @DisplayName("Should do nothing when friendship relation does not exist")
        void shouldDoNothing_whenRelationNotFound() {
            Long currentUserId = 1L;
            Long friendId = 2L;

            when(friendRelationRepository.findFriendRelationByUserId1AndUserId2(1L, 2L))
                    .thenReturn(Optional.empty());

            friendService.removeFriend(currentUserId, friendId);

            verify(friendRelationRepository, never()).delete(any());
        }
    }

    @Nested
    class GetUserFriends {

        @Test
        @DisplayName("Should return fallback UserRefDto when user details are missing in UserService")
        void shouldReturnFallbackUserRef_whenUserDetailIsMissing() {
            Long userId = 1L;
            Long friendId = 10L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Long> friendIdsPage = new PageImpl<>(List.of(friendId));

            when(friendRelationRepository.findAllFriendIdsByUserId(userId, pageable)).thenReturn(friendIdsPage);
            when(userService.getUserReferencesByIds(Set.of(friendId))).thenReturn(Map.of());

            Page<UserRefDto> result = friendService.getUserFriends(userId, pageable);

            assertThat(result.getContent()).hasSize(1);
            UserRefDto fallbackDto = result.getContent().get(0);
            assertThat(fallbackDto.userId()).isEqualTo(friendId);
            assertThat(fallbackDto.username()).isNull();
            assertThat(fallbackDto.displayName()).isEqualTo("User_"+friendId);
            assertThat(fallbackDto.avatarId()).isEqualTo(1);
        }
    }
}
