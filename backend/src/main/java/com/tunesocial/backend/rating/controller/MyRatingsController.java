package com.tunesocial.backend.rating.controller;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import com.tunesocial.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/me/ratings")
@RequiredArgsConstructor
public class MyRatingsController {

    private final RatingService ratingService;
    private final UserService userService;

    @GetMapping("/{targetType}/{targetId}")
    public RatingResponse getMyRatingForTarget(@PathVariable String targetId,
                                               @PathVariable RatingTargetType targetType,
                                               Authentication authentication) {
        Long currentUserId = userService.getCurrentUserIdOrThrow(authentication);

        return ratingService.findUserRatingForTarget(currentUserId, targetId, targetType)
                .map(RatingResponse::fromEntity)
                .orElseThrow(() ->
                        new RatingNotFoundException(currentUserId, targetId, targetType)
                );
    }

    @GetMapping()
    public List<RatingResponse> getMyRatings(Authentication authentication) {

        Long currentUserId = userService.getCurrentUserIdOrThrow(authentication);

        List<Rating> ratings = ratingService.getRatingsForUser(currentUserId);

        return ratings.stream()
                .map(RatingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/reviews")
    public ResponseEntity<PagedResponse<RatingResponse>> getMyReviews(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long currentUserId = userService.getCurrentUserIdOrThrow(authentication);

        return ResponseEntity.ok(ratingService.getUserComments(currentUserId, pageable));
    }
}
