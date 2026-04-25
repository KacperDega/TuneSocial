package com.tunesocial.backend.post.service;

import com.tunesocial.backend.post.dto.ReactionsSummary;
import com.tunesocial.backend.post.model.Reaction;
import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.model.enums.ReactionType;
import com.tunesocial.backend.post.event.ReactionAddedEvent;
import com.tunesocial.backend.post.repository.ReactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReactionService reactionService;

    @Nested
    class ToggleReaction {

        @Test
        @DisplayName("Should create new reaction and publish event when reaction does not already exist")
        void shouldCreateReactionAndPublishEvent_whenNoExistingReaction() {
            // Given
            Long userId = 1L;
            Long targetId = 100L;
            ReactionTargetType targetType = ReactionTargetType.POST;
            ReactionType reactionType = ReactionType.LIKE;

            when(reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType))
                    .thenReturn(Optional.empty());

            // When
            reactionService.toggleReaction(userId, targetId, targetType, reactionType);

            // Then
            ArgumentCaptor<Reaction> reactionCaptor = ArgumentCaptor.forClass(Reaction.class);
            verify(reactionRepository).save(reactionCaptor.capture());

            Reaction savedReaction = reactionCaptor.getValue();
            assertThat(savedReaction.getUserId()).isEqualTo(userId);
            assertThat(savedReaction.getTargetId()).isEqualTo(targetId);
            assertThat(savedReaction.getTargetType()).isEqualTo(targetType);
            assertThat(savedReaction.getType()).isEqualTo(reactionType);

            ArgumentCaptor<ReactionAddedEvent> eventCaptor = ArgumentCaptor.forClass(ReactionAddedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            ReactionAddedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.actorId()).isEqualTo(userId);
            assertThat(publishedEvent.targetType()).isEqualTo(targetType);
            assertThat(publishedEvent.targetId()).isEqualTo(targetId.toString());
        }

        @Test
        @DisplayName("Should delete reaction and not publish event when toggling the same reaction type")
        void shouldDeleteReactionAndNotPublishEvent_whenSameReactionTypeToggled() {
            // Given
            Long userId = 1L;
            Long targetId = 100L;
            ReactionTargetType targetType = ReactionTargetType.POST;
            ReactionType reactionType = ReactionType.LIKE;

            Reaction existingReaction = new Reaction();
            existingReaction.setId(50L);
            existingReaction.setUserId(userId);
            existingReaction.setTargetId(targetId);
            existingReaction.setTargetType(targetType);
            existingReaction.setType(reactionType);

            when(reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType))
                    .thenReturn(Optional.of(existingReaction));

            // When
            reactionService.toggleReaction(userId, targetId, targetType, reactionType);

            // Then
            verify(reactionRepository).delete(existingReaction);
            verify(reactionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Should update reaction type and publish event when changing to a different reaction type")
        void shouldUpdateReactionTypeAndPublishEvent_whenDifferentReactionTypeToggled() {
            // Given
            Long userId = 1L;
            Long targetId = 100L;
            ReactionTargetType targetType = ReactionTargetType.POST;

            Reaction existingReaction = new Reaction();
            existingReaction.setId(50L);
            existingReaction.setUserId(userId);
            existingReaction.setTargetId(targetId);
            existingReaction.setTargetType(targetType);
            existingReaction.setType(ReactionType.LIKE);

            when(reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType))
                    .thenReturn(Optional.of(existingReaction));

            // When
            reactionService.toggleReaction(userId, targetId, targetType, ReactionType.HAHA);

            // Then
            verify(reactionRepository).save(existingReaction);
            assertThat(existingReaction.getType()).isEqualTo(ReactionType.HAHA);

            ArgumentCaptor<ReactionAddedEvent> eventCaptor = ArgumentCaptor.forClass(ReactionAddedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            ReactionAddedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.actorId()).isEqualTo(userId);
            assertThat(publishedEvent.targetType()).isEqualTo(targetType);
            assertThat(publishedEvent.targetId()).isEqualTo(targetId.toString());
        }
    }

    @Nested
    class RemoveReactionsForTargets {

        @Test
        @DisplayName("Should delete reactions when targetIds list is valid and non-empty")
        void shouldDeleteReactions_whenTargetIdsProvided() {
            // Given
            ReactionTargetType targetType = ReactionTargetType.COMMENT;
            Collection<Long> targetIds = List.of(10L, 20L, 30L);

            // When
            reactionService.removeReactionsForTargets(targetType, targetIds);

            // Then
            verify(reactionRepository).deleteAllByTargetTypeAndTargetIdIn(targetType, targetIds);
        }

        @Test
        @DisplayName("Should do nothing when targetIds collection is null or empty")
        void shouldDoNothing_whenTargetIdsIsNullOrEmpty() {
            // Given
            ReactionTargetType targetType = ReactionTargetType.COMMENT;

            // When
            reactionService.removeReactionsForTargets(targetType, null);
            reactionService.removeReactionsForTargets(targetType, List.of());

            // Then
            verify(reactionRepository, never()).deleteAllByTargetTypeAndTargetIdIn(any(), any());
        }
    }

    @Nested
    class GetReactionSummary {

        @Test
        @DisplayName("Should return summary with total count, counts by type and current user reaction")
        void shouldReturnSummaryWithCurrentUserReaction_whenUserIsLoggedIn() {
            // Given
            Long targetId = 100L;
            ReactionTargetType targetType = ReactionTargetType.POST;
            Long currentUserId = 1L;

            ReactionRepository.ReactionCount likeCount = mock(ReactionRepository.ReactionCount.class);
            when(likeCount.getType()).thenReturn(ReactionType.LIKE);
            when(likeCount.getCount()).thenReturn(5L);

            ReactionRepository.ReactionCount loveCount = mock(ReactionRepository.ReactionCount.class);
            when(loveCount.getType()).thenReturn(ReactionType.HAHA);
            when(loveCount.getCount()).thenReturn(2L);

            Reaction userReaction = new Reaction();
            userReaction.setType(ReactionType.LIKE);

            when(reactionRepository.countByTarget(targetId, targetType))
                    .thenReturn(List.of(likeCount, loveCount));
            when(reactionRepository.findByUserIdAndTargetIdAndTargetType(currentUserId, targetId, targetType))
                    .thenReturn(Optional.of(userReaction));

            // When
            ReactionsSummary summary = reactionService.getReactionSummary(targetId, targetType, currentUserId);

            // Then
            assertThat(summary.totalCount()).isEqualTo(7L);
            assertThat(summary.countsByType())
                    .containsEntry(ReactionType.LIKE, 5L)
                    .containsEntry(ReactionType.HAHA, 2L);
            assertThat(summary.myReaction()).isEqualTo(ReactionType.LIKE);
        }

        @Test
        @DisplayName("Should return summary with null for user reaction when user is not logged in")
        void shouldReturnSummaryWithNullUserReaction_whenUserIdIsNull() {
            // Given
            Long targetId = 100L;
            ReactionTargetType targetType = ReactionTargetType.POST;

            ReactionRepository.ReactionCount likeCount = mock(ReactionRepository.ReactionCount.class);
            when(likeCount.getType()).thenReturn(ReactionType.LIKE);
            when(likeCount.getCount()).thenReturn(3L);

            when(reactionRepository.countByTarget(targetId, targetType))
                    .thenReturn(List.of(likeCount));

            // When
            ReactionsSummary summary = reactionService.getReactionSummary(targetId, targetType, null);

            // Then
            assertThat(summary.totalCount()).isEqualTo(3L);
            assertThat(summary.countsByType()).containsEntry(ReactionType.LIKE, 3L);
            assertThat(summary.myReaction()).isNull();
            verify(reactionRepository, never()).findByUserIdAndTargetIdAndTargetType(any(), any(), any());
        }
    }
}
