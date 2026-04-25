package com.tunesocial.backend.post.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.post.dto.CommentResponse;
import com.tunesocial.backend.post.dto.CreateCommentRequest;
import com.tunesocial.backend.post.model.PostComment;
import com.tunesocial.backend.post.event.CommentCreatedEvent;
import com.tunesocial.backend.post.exception.InvalidParentCommentException;
import com.tunesocial.backend.post.exception.SocialResourceNotFoundException;
import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.repository.PostCommentRepository;
import com.tunesocial.backend.post.dto.ReactionsSummary;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostCommentRepository commentRepository;

    @Mock
    private ReactionService reactionService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    @Nested
    class GetCommentsForPost {

        @Test
        @DisplayName("Should return empty page response when post has no comments")
        void shouldReturnEmptyPage_whenNoCommentsFound() {
            // Given
            Long postId = 1L;
            Long currentUserId = 10L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostComment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(commentRepository.findAllByPostIdAndParentIdIsNull(postId, pageable)).thenReturn(emptyPage);

            // When
            PagedResponse<CommentResponse> result = commentService.getCommentsForPost(postId, pageable, currentUserId);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.nextPage()).isNull();
        }

        @Test
        @DisplayName("Should map comments with author details and reactions summary")
        void shouldMapComments_whenCommentsExist() {
            // Given
            Long postId = 1L;
            Long currentUserId = 10L;
            Long authorId = 20L;
            Long commentId = 100L;
            Pageable pageable = PageRequest.of(0, 10);

            PostComment comment = new PostComment();
            comment.setId(commentId);
            comment.setPostId(postId);
            comment.setUserId(authorId);
            comment.setContent("Hello world");
            comment.setCreatedAt(Instant.now());

            Page<PostComment> commentPage = new PageImpl<>(List.of(comment), pageable, 1);
            UserRefDto userRef = new UserRefDto(authorId, "user1", "user_1", 1);
            ReactionsSummary reactionsSummary = new ReactionsSummary(5, Map.of(), null);

            when(commentRepository.findAllByPostIdAndParentIdIsNull(postId, pageable)).thenReturn(commentPage);
            when(userService.getUserReferencesByIds(Set.of(authorId))).thenReturn(Map.of(authorId, userRef));
            when(reactionService.getReactionSummary(commentId, ReactionTargetType.COMMENT, currentUserId)).thenReturn(reactionsSummary);
            when(commentRepository.countByParentId(commentId)).thenReturn(2L);

            // When
            PagedResponse<CommentResponse> result = commentService.getCommentsForPost(postId, pageable, currentUserId);

            // Then
            assertThat(result.content()).hasSize(1);
            CommentResponse response = result.content().get(0);
            assertThat(response.id()).isEqualTo(commentId);
            assertThat(response.author().username()).isEqualTo("user1");
            assertThat(response.content()).isEqualTo("Hello world");
            assertThat(response.repliesCount()).isEqualTo(2L);
        }
    }

    @Nested
    class GetRepliesForComment {

        @Test
        @DisplayName("Should return mapped replies for parent comment ID")
        void shouldReturnRepliesList_whenRepliesExist() {
            // Given
            Long parentId = 100L;
            Long currentUserId = 10L;
            Long replyAuthorId = 30L;
            Long replyId = 200L;

            PostComment reply = new PostComment();
            reply.setId(replyId);
            reply.setParentId(parentId);
            reply.setUserId(replyAuthorId);
            reply.setContent("Reply content");

            UserRefDto userRef = new UserRefDto(replyAuthorId, "replyUser", "reply_user", 1);
            ReactionsSummary reactionsSummary = new ReactionsSummary(1, Map.of(), null);

            when(commentRepository.findAllByParentIdOrderByCreatedAtAsc(parentId)).thenReturn(List.of(reply));
            when(userService.getUserReferencesByIds(Set.of(replyAuthorId))).thenReturn(Map.of(replyAuthorId, userRef));
            when(reactionService.getReactionSummary(replyId, ReactionTargetType.COMMENT, currentUserId)).thenReturn(reactionsSummary);

            // When
            List<CommentResponse> replies = commentService.getRepliesForComment(parentId, currentUserId);

            // Then
            assertThat(replies).hasSize(1);
            assertThat(replies.get(0).id()).isEqualTo(replyId);
            assertThat(replies.get(0).parentId()).isEqualTo(parentId);
            assertThat(replies.get(0).author().username()).isEqualTo("replyUser");
        }
    }

    @Nested
    class AddComment {

        @Test
        @DisplayName("Should throw SocialResourceNotFoundException when parent comment does not exist")
        void shouldThrowException_whenParentCommentNotFound() {
            // Given
            Long userId = 10L;
            CreateCommentRequest request = new CreateCommentRequest(1L, "Content", 99L);

            when(commentRepository.findById(99L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> commentService.addComment(userId, request))
                    .isInstanceOf(SocialResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw InvalidParentCommentException when parent comment belongs to a different post")
        void shouldThrowException_whenParentBelongsToDifferentPost() {
            // Given
            Long userId = 10L;
            Long postId = 1L;
            Long otherPostId = 2L;

            PostComment parentComment = new PostComment();
            parentComment.setId(99L);
            parentComment.setPostId(otherPostId);

            CreateCommentRequest request = new CreateCommentRequest(postId, "Content", 99L);

            when(commentRepository.findById(99L)).thenReturn(Optional.of(parentComment));

            // When / Then
            assertThatThrownBy(() -> commentService.addComment(userId, request))
                    .isInstanceOf(InvalidParentCommentException.class);
        }

        @Test
        @DisplayName("Should flatten nested reply to top parent ID")
        void shouldFlattenParentId_whenReplyingToNestedComment() {
            // Given
            Long userId = 10L;
            Long postId = 1L;
            Long rootParentId = 50L;
            Long nestedCommentId = 99L;

            PostComment nestedComment = new PostComment();
            nestedComment.setId(nestedCommentId);
            nestedComment.setPostId(postId);
            nestedComment.setParentId(rootParentId);

            CreateCommentRequest request = new CreateCommentRequest(postId, "Nested reply", nestedCommentId);

            PostComment savedComment = new PostComment();
            savedComment.setId(101L);
            savedComment.setPostId(postId);
            savedComment.setUserId(userId);
            savedComment.setContent("Nested reply");
            savedComment.setParentId(rootParentId);

            when(commentRepository.findById(nestedCommentId)).thenReturn(Optional.of(nestedComment));
            when(commentRepository.save(any(PostComment.class))).thenReturn(savedComment);
            when(userService.getUserReferencesByIds(Set.of(userId))).thenReturn(Map.of());

            // When
            CommentResponse response = commentService.addComment(userId, request);

            // Then
            assertThat(response.parentId()).isEqualTo(rootParentId);

            ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
            verify(commentRepository).save(commentCaptor.capture());
            assertThat(commentCaptor.getValue().getParentId()).isEqualTo(rootParentId);
        }

        @Test
        @DisplayName("Should publish CommentCreatedEvent when comment is created")
        void shouldPublishEvent_whenCommentAdded() {
            // Given
            Long userId = 10L;
            Long postId = 1L;
            String commentContent = "New comment";
            CreateCommentRequest request = new CreateCommentRequest(postId, commentContent, null);

            PostComment savedComment = new PostComment();
            savedComment.setId(101L);
            savedComment.setPostId(postId);
            savedComment.setUserId(userId);
            savedComment.setContent(commentContent);

            when(commentRepository.save(any(PostComment.class))).thenReturn(savedComment);
            when(userService.getUserReferencesByIds(Set.of(userId))).thenReturn(Map.of());

            // When
            commentService.addComment(userId, request);

            // Then
            ArgumentCaptor<CommentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CommentCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            CommentCreatedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.commentId()).isEqualTo(101L);
            assertThat(publishedEvent.postId()).isEqualTo(postId);
            assertThat(publishedEvent.actorId()).isEqualTo(userId);
            assertThat(publishedEvent.parentCommentId()).isNull();
            assertThat(publishedEvent.commentContent()).isEqualTo("New comment");
        }
    }

    @Nested
    class DeleteComment {

        @Test
        @DisplayName("Should throw SocialResourceNotFoundException when comment to delete does not exist")
        void shouldThrowException_whenCommentNotFound() {
            // Given
            Long commentId = 1L;
            Long userId = 10L;

            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, userId))
                    .isInstanceOf(SocialResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user tries to delete someone else's comment")
        void shouldThrowException_whenUserIsNotOwner() {
            // Given
            Long commentId = 1L;
            Long ownerId = 20L;
            Long currentUserId = 10L;

            PostComment comment = new PostComment();
            comment.setId(commentId);
            comment.setUserId(ownerId);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // When / Then
            assertThatThrownBy(() -> commentService.deleteComment(commentId, currentUserId))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should delete main comment and all its replies along with reactions")
        void shouldDeleteCommentAndReplies_whenParentCommentIsDeleted() {
            // Given
            Long commentId = 100L;
            Long userId = 10L;
            List<Long> replyIds = List.of(101L, 102L);

            PostComment comment = new PostComment();
            comment.setId(commentId);
            comment.setUserId(userId);
            comment.setParentId(null);

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.findAllIdsByParentId(commentId)).thenReturn(replyIds);

            // When
            commentService.deleteComment(commentId, userId);

            // Then
            verify(commentRepository).deleteAllByParentId(commentId);
            verify(reactionService).removeReactionsForTargets(ReactionTargetType.COMMENT, List.of(100L, 101L, 102L));
            verify(commentRepository).delete(comment);
        }
    }
}
