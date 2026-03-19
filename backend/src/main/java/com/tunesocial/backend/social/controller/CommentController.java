package com.tunesocial.backend.social.controller;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.social.dto.CommentResponse;
import com.tunesocial.backend.social.dto.CreateCommentRequest;
import com.tunesocial.backend.social.service.CommentService;
import com.tunesocial.backend.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<PagedResponse<CommentResponse>> getPostComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable) {

        Long currentUserId = (user != null) ? user.getId() : null;
        return ResponseEntity.ok(commentService.getCommentsForPost(postId, pageable, currentUserId));
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getCommentReplies(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {

        Long currentUserId = (user != null) ? user.getId() : null;
        return ResponseEntity.ok(commentService.getRepliesForComment(commentId, currentUserId));
    }
    
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCommentRequest request) {

        return ResponseEntity.ok(commentService.addComment(user.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        commentService.deleteComment(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
