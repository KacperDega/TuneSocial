package com.tunesocial.backend.social.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.social.dto.CommentResponse;
import com.tunesocial.backend.social.dto.CreateCommentRequest;
import com.tunesocial.backend.social.dto.ReactionsSummary;
import com.tunesocial.backend.social.exception.InvalidParentCommentException;
import com.tunesocial.backend.social.exception.SocialResourceNotFoundException;
import com.tunesocial.backend.social.model.PostComment;
import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.repository.PostCommentRepository;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PostCommentRepository commentRepository;
    private final SocialService socialService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getCommentsForPost(Long postId, Pageable pageable, Long currentUserId) {
        Page<PostComment> commentsPage = commentRepository.findAllByPostIdAndParentIdIsNull(postId, pageable);
        List<PostComment> comments = commentsPage.getContent();

        Set<Long> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, UserRefDto> userRefs = userService.getUserReferencesByIds(userIds);

        List<CommentResponse> responses = comments.stream().map(c -> {
            ReactionsSummary reactions = socialService.getReactionSummary(c.getId(), ReactionTargetType.COMMENT, currentUserId);
            long repliesCount = commentRepository.countByParentId(c.getId());

            UserRefDto author = userRefs.getOrDefault(
                    c.getUserId(),
                    new UserRefDto(c.getUserId(), null, "User_" + c.getUserId(), 1)
            );

            return new CommentResponse(
                    c.getId(),
                    author,
                    c.getContent(),
                    c.getParentId(),
                    reactions,
                    repliesCount,
                    c.getCreatedAt(),
                    List.of()
            );
        }).toList();

        return new PagedResponse<>(responses, commentsPage.hasNext() ? commentsPage.getNumber() + 1 : null);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesForComment(Long commentId, Long currentUserId) {
        List<PostComment> replies = commentRepository.findAllByParentIdOrderByCreatedAtAsc(commentId);

        Set<Long> userIds = replies.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, UserRefDto> userRefs = userService.getUserReferencesByIds(userIds);

        return replies.stream().map(r -> {
            ReactionsSummary reactions = socialService.getReactionSummary(r.getId(), ReactionTargetType.COMMENT, currentUserId);

            UserRefDto author = userRefs.getOrDefault(
                    r.getUserId(),
                    new UserRefDto(r.getUserId(), null, "User_" + r.getUserId(), 1)
            );

            return new CommentResponse(
                    r.getId(),
                    author,
                    r.getContent(),
                    r.getParentId(),
                    reactions,
                    0L,
                    r.getCreatedAt(),
                    List.of()
            );
        }).toList();
    }

    @Transactional
    public CommentResponse addComment(Long userId, CreateCommentRequest request) {
        Long targetParentId = request.parentId();

        // reply
        if (targetParentId != null) {
            PostComment parentComment = commentRepository.findById(targetParentId)
                    .orElseThrow(() -> new SocialResourceNotFoundException("Parent comment not found"));

            if (!parentComment.getPostId().equals(request.postId())) {
                throw new InvalidParentCommentException("Parent comment does not belong to the specified post");
            }

            if (parentComment.getParentId() != null) {
                targetParentId = parentComment.getParentId();
            }
        }

        PostComment comment = new PostComment();
        comment.setPostId(request.postId());
        comment.setUserId(userId);
        comment.setContent(request.content());
        comment.setParentId(targetParentId);

        PostComment saved = commentRepository.save(comment);

        Map<Long, UserRefDto> userRefs = userService.getUserReferencesByIds(Set.of(userId));

        UserRefDto author = userRefs.getOrDefault(
                userId,
                new UserRefDto(userId, null, "User_" + userId, 1)
        );

        return new CommentResponse(
                saved.getId(),
                author,
                saved.getContent(),
                saved.getParentId(),
                new ReactionsSummary(0, Map.of(), null),
                0,
                saved.getCreatedAt(),
                List.of()
        );
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SocialResourceNotFoundException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        // if parent comment
        if (comment.getParentId() == null) {
            commentRepository.deleteAllByParentId(commentId);
        }

        commentRepository.delete(comment);
    }
}
