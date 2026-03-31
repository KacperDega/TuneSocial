package com.tunesocial.backend.rating.controller;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.rating.dto.RatingDetailsResponse;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import com.tunesocial.backend.user.model.User;
import com.tunesocial.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                                               @AuthenticationPrincipal User user) {
        Long currentUserId = user.getId();

        return ratingService.findUserRatingForTarget(currentUserId, targetId, targetType)
                .map(RatingResponse::fromEntity)
                .orElseThrow(() ->
                        new RatingNotFoundException(currentUserId, targetId, targetType)
                );
    }

    @GetMapping
    public ResponseEntity<PagedResponse<RatingDetailsResponse>> getMyRatings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();

        PagedResponse<RatingDetailsResponse> response = ratingService.getRatingsForUser(currentUserId, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews")
    public ResponseEntity<PagedResponse<RatingDetailsResponse>> getMyReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) RatingTargetType type,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long currentUserId = user.getId();

        return ResponseEntity.ok(ratingService.getUserComments(currentUserId, type, pageable));
    }
}
