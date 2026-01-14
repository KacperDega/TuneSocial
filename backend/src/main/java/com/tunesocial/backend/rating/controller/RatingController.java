package com.tunesocial.backend.rating.controller;

import com.tunesocial.backend.rating.dto.RateRequest;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.dto.RatingSummaryResponse;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import com.tunesocial.backend.user.User;
import com.tunesocial.backend.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> rate(@RequestBody @Valid RateRequest request, Authentication authentication) {
        Long currentUserId = userService.getCurrentUserId(authentication);

        ratingService.rate(currentUserId, request.targetId(), request.targetType(), request.value());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeRating(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getCurrentUserId(authentication);

        ratingService.removeRating(currentUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public RatingSummaryResponse getSummary(@RequestParam String targetId,
                                            @RequestParam RatingTargetType targetType) {

        RatingSummary summary = ratingService.getSummaryForTarget(targetId, targetType);
        return RatingSummaryResponse.fromEntity(summary);
    }
}
