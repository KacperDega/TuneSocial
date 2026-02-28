package com.tunesocial.backend.rating.controller;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.rating.dto.RateRequest;
import com.tunesocial.backend.rating.dto.RatingDetailsResponse;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.dto.RatingSummaryResponse;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import com.tunesocial.backend.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> rate(@RequestBody @Valid RateRequest request, Authentication authentication) {
        Long currentUserId = userService.getCurrentUserIdOrThrow(authentication);

        ratingService.rate(currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeRating(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = userService.getCurrentUserIdOrThrow(authentication);

        ratingService.removeRating(currentUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public RatingSummaryResponse getSummary(@RequestParam String targetId,
                                            @RequestParam RatingTargetType targetType) {

        RatingSummary summary = ratingService.getSummaryForTarget(targetId, targetType);
        return RatingSummaryResponse.fromEntity(summary);
    }

    @GetMapping("/reviews/{type}/{targetId}")
    public ResponseEntity<PagedResponse<RatingResponse>> getReviews(
            @PathVariable RatingTargetType type,
            @PathVariable String targetId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ratingService.getCommentsPageForTarget(targetId, type, pageable));
    }

    @GetMapping("/reviews")
    public ResponseEntity<PagedResponse<RatingDetailsResponse>> getGlobalReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ratingService.getGlobalReviews(pageable));
    }
}
