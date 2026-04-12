package com.tunesocial.backend.post.controller;

import com.tunesocial.backend.post.dto.ReactionRequest;
import com.tunesocial.backend.post.dto.ReactionsSummary;
import com.tunesocial.backend.post.model.enums.ReactionTargetType;
import com.tunesocial.backend.post.service.ReactionService;
import com.tunesocial.backend.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reaction")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/toggle")
    public ResponseEntity<Void> react(@Valid @RequestBody ReactionRequest request,
                                      @AuthenticationPrincipal User user)
    {
        reactionService.toggleReaction(
                user.getId(),
                request.targetId(),
                request.targetType(),
                request.reactionType()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<ReactionsSummary> getReactionStats(
            @PathVariable ReactionTargetType targetType,
            @PathVariable Long targetId,
            @AuthenticationPrincipal User user)
    {
        return ResponseEntity.ok(reactionService.getReactionSummary(targetId, targetType, user.getId()));
    }
}
