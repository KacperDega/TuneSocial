package com.tunesocial.backend.user.controller;

import com.tunesocial.backend.user.dto.SetupProfileRequest;
import com.tunesocial.backend.user.service.UserProfileService;
import com.tunesocial.backend.user.dto.UpdateProfileRequest;
import com.tunesocial.backend.user.dto.UserProfileResponse;
import com.tunesocial.backend.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getProfile(
            @PathVariable String username,
            @AuthenticationPrincipal User user) {

        Long currentUserId = (user != null) ? user.getId() : null;
        return ResponseEntity.ok(profileService.getProfileByUsername(username, currentUserId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(profileService.updateProfile(user.getId(), request));
    }

    @PostMapping("/me/setup")
    public ResponseEntity<UserProfileResponse> setupProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SetupProfileRequest request) {

        return ResponseEntity.ok(profileService.setupProfile(user.getId(), request));
    }
}
