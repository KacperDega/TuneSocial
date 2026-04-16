package com.tunesocial.backend.relation.controller;

import com.tunesocial.backend.relation.service.FriendService;
import com.tunesocial.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/requests/send/{recipientId}")
    public ResponseEntity<Void> sendFriendRequest(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long recipientId) {
        friendService.sendFriendRequest(currentUser.getId(), recipientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long requestId) {
        friendService.acceptFriendRequest(currentUser.getId(), requestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<Void> cancelOrRejectFriendRequest(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long requestId) {
        friendService.cancelOrRejectFriendRequest(currentUser.getId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long friendId) {
        friendService.removeFriend(currentUser.getId(), friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/friends/users/{userId}/count")
    public ResponseEntity<Long> getFriendCount(@PathVariable Long userId) {
        long count = friendService.getFriendCount(userId);
        return ResponseEntity.ok(count);
    }
}
