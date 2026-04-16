package com.tunesocial.backend.relation.controller;

import com.tunesocial.backend.relation.dto.FollowStatsDto;
import com.tunesocial.backend.relation.service.FollowService;
import com.tunesocial.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followingId}")
    public ResponseEntity<Void> followUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long followingId) {
        followService.followUser(currentUser.getId(), followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<Void> unfollowUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long followingId) {
        followService.unfollowUser(currentUser.getId(), followingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{targetUserId}")
    public ResponseEntity<Boolean> isFollowing(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long targetUserId) {
        boolean isFollowing = followService.isFollowing(currentUser.getId(), targetUserId);
        return ResponseEntity.ok(isFollowing);
    }

    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<FollowStatsDto> getFollowStats(@PathVariable Long userId) {
        long followers = followService.getFollowersCount(userId);
        long following = followService.getFollowingCount(userId);
        return ResponseEntity.ok(new FollowStatsDto(followers, following));
    }
}
