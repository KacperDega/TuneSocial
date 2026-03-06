package com.tunesocial.backend.social.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.social.dto.CommentResponse;
import com.tunesocial.backend.social.dto.CreateCommentRequest;
import com.tunesocial.backend.social.dto.ReactionsSummary;
import com.tunesocial.backend.social.exception.SocialResourceNotFoundException;
import com.tunesocial.backend.social.model.PostComment;
import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.repository.PostCommentRepository;
import com.tunesocial.backend.user.UserService;
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
        Map<Long, String> usernames = userService.getUsernamesByIds(userIds);

        List<CommentResponse> responses = comments.stream().map(c -> {
            ReactionsSummary reactions = socialService.getReactionSummary(c.getId(), ReactionTargetType.COMMENT, currentUserId);
            long repliesCount = commentRepository.countByParentId(c.getId());

            return new CommentResponse(
                    c.getId(),
                    c.getUserId(),
                    usernames.get(c.getUserId()),
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
        Map<Long, String> usernames = userService.getUsernamesByIds(userIds);

        return replies.stream().map(r -> {
            ReactionsSummary reactions = socialService.getReactionSummary(r.getId(), ReactionTargetType.COMMENT, currentUserId);
            return new CommentResponse(
                    r.getId(),
                    r.getUserId(),
                    usernames.getOrDefault(r.getUserId(), "User_" + r.getUserId()),
                    r.getContent(),
                    r.getParentId(),
                    reactions,
                    0L,
                    r.getCreatedAt(),
                    List.of()
            );
        }).toList();
    }

    // Dodawanie komentarza lub odpowiedzi
    @Transactional
    public CommentResponse addComment(Long userId, CreateCommentRequest request) {
        PostComment comment = new PostComment();
        comment.setPostId(request.postId());
        comment.setUserId(userId);
        comment.setContent(request.content());
        comment.setParentId(request.parentId());

        PostComment saved = commentRepository.save(comment);

        String username = userService.getUsernamesByIds(Set.of(userId)).get(userId);

        return new CommentResponse(
                saved.getId(),
                userId,
                username != null ? username : "User_" + userId,
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
