package com.tunesocial.backend.relation.service;

import com.tunesocial.backend.relation.model.FollowRelation;
import com.tunesocial.backend.relation.event.UserFollowedEvent;
import com.tunesocial.backend.relation.exception.AlreadyRelatedException;
import com.tunesocial.backend.relation.exception.SelfRelationException;
import com.tunesocial.backend.relation.repository.FollowRelationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRelationRepository followRelationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FollowService followService;

    @Nested
    class FollowUser {

        @Test
        @DisplayName("Should throw SelfRelationException when trying to follow self")
        void shouldThrowSelfRelationException_whenFollowerIsSameAsFollowing() {
            Long userId = 1L;

            assertThatThrownBy(() -> followService.followUser(userId, userId))
                    .isInstanceOf(SelfRelationException.class);

            verifyNoInteractions(followRelationRepository, eventPublisher);
        }

        @Test
        @DisplayName("Should throw AlreadyRelatedException when follow relation already exists")
        void shouldThrowAlreadyRelatedException_whenAlreadyFollowing() {
            Long followerId = 1L;
            Long followingId = 2L;

            when(followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

            assertThatThrownBy(() -> followService.followUser(followerId, followingId))
                    .isInstanceOf(AlreadyRelatedException.class);

            verify(followRelationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Should save follow relation and publish event when parameters are valid")
        void shouldSaveFollowAndPublishEvent_whenValid() {
            Long followerId = 1L;
            Long followingId = 2L;

            when(followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);

            followService.followUser(followerId, followingId);

            verify(followRelationRepository).save(any(FollowRelation.class));
            verify(eventPublisher).publishEvent(any(UserFollowedEvent.class));
        }
    }

    @Nested
    class UnfollowUser {

        @Test
        @DisplayName("Should delete relation when follow relation exists")
        void shouldDeleteRelation_whenExists() {
            Long followerId = 1L;
            Long followingId = 2L;
            FollowRelation relation = new FollowRelation(followerId, followingId);

            when(followRelationRepository.findByFollowerIdAndFollowingId(followerId, followingId))
                    .thenReturn(Optional.of(relation));

            followService.unfollowUser(followerId, followingId);

            verify(followRelationRepository).delete(relation);
        }

        @Test
        @DisplayName("Should do nothing when follow relation does not exist")
        void shouldDoNothing_whenRelationNotFound() {
            Long followerId = 1L;
            Long followingId = 2L;

            when(followRelationRepository.findByFollowerIdAndFollowingId(followerId, followingId))
                    .thenReturn(Optional.empty());

            followService.unfollowUser(followerId, followingId);

            verify(followRelationRepository, never()).delete(any());
        }
    }

    @Nested
    class EnsureFollow {

        @Test
        @DisplayName("Should return and not throw when follower is same as following")
        void shouldDoNothing_whenFollowerIsSameAsFollowing() {
            Long userId = 1L;

            followService.ensureFollow(userId, userId);

            verifyNoInteractions(followRelationRepository);
        }

        @Test
        @DisplayName("Should save follow relation when not already following")
        void shouldSave_whenNotAlreadyFollowing() {
            Long followerId = 1L;
            Long followingId = 2L;

            when(followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);

            followService.ensureFollow(followerId, followingId);

            verify(followRelationRepository).save(any(FollowRelation.class));
        }

        @Test
        @DisplayName("Should not save follow relation when already following")
        void shouldNotSave_whenAlreadyFollowing() {
            Long followerId = 1L;
            Long followingId = 2L;

            when(followRelationRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

            followService.ensureFollow(followerId, followingId);

            verify(followRelationRepository, never()).save(any());
        }
    }
}
