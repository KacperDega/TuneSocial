package com.tunesocial.backend.relation.controller;

import com.tunesocial.backend.relation.dto.FriendRequestDto;
import com.tunesocial.backend.relation.service.FriendService;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/requests")
    public ResponseEntity<Page<FriendRequestDto>> getFriendRequests(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(friendService.getFriendRequests(user.getId(), pageable));
    }

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

    // ==========================

    @GetMapping("/{userId}/friends")
    public ResponseEntity<Page<UserRefDto>> getUserFriends(
            @PathVariable Long userId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<UserRefDto> friends = friendService.getUserFriends(userId, pageable);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long friendId) {
        friendService.removeFriend(currentUser.getId(), friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<Long> getFriendCount(@PathVariable Long userId) {
        long count = friendService.getFriendCount(userId);
        return ResponseEntity.ok(count);
    }
}
