package com.tunesocial.backend.social.controller;

import com.tunesocial.backend.social.dto.ReactionRequest;
import com.tunesocial.backend.social.dto.ReactionsSummary;
import com.tunesocial.backend.social.model.enums.ReactionTargetType;
import com.tunesocial.backend.social.service.SocialService;
import com.tunesocial.backend.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @PostMapping("/react")
    public ResponseEntity<Void> react(@Valid @RequestBody ReactionRequest request,
                                      @AuthenticationPrincipal User user)
    {
        socialService.toggleReaction(
                user.getId(),
                request.targetId(),
                request.targetType(),
                request.reactionType()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reactions/{targetType}/{targetId}")
    public ResponseEntity<ReactionsSummary> getReactionStats(
            @PathVariable ReactionTargetType targetType,
            @PathVariable Long targetId,
            @AuthenticationPrincipal User user)
    {
        return ResponseEntity.ok(socialService.getReactionSummary(targetId, targetType, user.getId()));
    }
}
