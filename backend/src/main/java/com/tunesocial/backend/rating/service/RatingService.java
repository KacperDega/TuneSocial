package com.tunesocial.backend.rating.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.RateableEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.service.MusicMetadataService;
import com.tunesocial.backend.rating.dto.RateRequest;
import com.tunesocial.backend.rating.dto.RatingDetailsResponse;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.exception.RatingSummaryNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.repository.RatingRepository;
import com.tunesocial.backend.rating.repository.RatingSummaryRepository;
import com.tunesocial.backend.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RatingSummaryRepository summaryRepository;
    private final MusicMetadataService metadataService;
    private final UserService userService;


    // TODO: optionally validate external target existence via provider
    @Transactional
    public void rate(Long userId, RateRequest request) {
        Optional<Rating> existingOpt =
                ratingRepository.findByUserIdAndTargetIdAndTargetType(userId, request.targetId(), request.targetType());

        summaryRepository
                .findByTargetIdAndTargetType(request.targetId(), request.targetType())
                .orElseGet(() -> createSummary(request.targetId(), request.targetType()));

        if (existingOpt.isPresent()) {
            updateExistingRating(existingOpt.get(), request.value(), request.comment());
        } else {
            createNewRating(userId, request);
        }
    }

    @Transactional
    public void removeRating(Long currentUserId, Long id) {

        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new RatingNotFoundException(id));

        if (!rating.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Cannot delete other user's rating");
        }

        RatingSummary summary = summaryRepository
                .findByTargetIdAndTargetType(rating.getTargetId(), rating.getTargetType())
                .orElseThrow(() ->
                        new RatingSummaryNotFoundException(
                                rating.getTargetId(),
                                rating.getTargetType()
                        )
                );

        summaryRepository.updateSummary(summary.getTargetId(), summary.getTargetType(), -1, -rating.getRatingValue());
        ratingRepository.delete(rating);
    }

    @Transactional(readOnly = true)
    public Optional<Rating> findUserRatingForTarget(Long userId, String targetId, RatingTargetType type) {

        return ratingRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, type);
    }

    @Transactional(readOnly = true)
    public RatingSummary getSummaryForTarget(String targetId, RatingTargetType type) {

        return summaryRepository
                .findByTargetIdAndTargetType(targetId, type)
                .orElseGet(() -> {
                    RatingSummary empty = new RatingSummary();
                    empty.setTargetId(targetId);
                    empty.setTargetType(type);
                    empty.setRatingCount(0);
                    empty.setRatingSum(0);
                    return empty;
                });
    }

    @Transactional(readOnly = true)
    public List<Rating> getRatingsForUser(Long userId) {
        return ratingRepository.findAllByUserId((userId));
    }

    private RatingSummary createSummary(String targetId, RatingTargetType type) {
        RatingSummary summary = new RatingSummary();
        summary.setTargetId(targetId);
        summary.setTargetType(type);
        summary.setRatingCount(0);
        summary.setRatingSum(0);

        return summaryRepository.save(summary);
    }

    private void createNewRating(Long userId, RateRequest request) {
        Rating rating = new Rating();
        rating.setUserId(userId);
        rating.setTargetId(request.targetId());
        rating.setTargetType(request.targetType());
        rating.setRatingValue(request.value());
        rating.setComment(request.comment());

        rating.setCreatedAt(Instant.now());
        rating.setUpdatedAt(rating.getCreatedAt());

        ratingRepository.save(rating);

        summaryRepository.updateSummary(request.targetId(), request.targetType(), 1, request.value());
    }

    private void updateExistingRating(Rating rating, int newValue, String comment) {
        int oldValue = rating.getRatingValue();
        rating.setRatingValue(newValue);

        if (rating.getComment() == null || !rating.getComment().equals(comment)) {
            rating.setComment(comment);
        }

        ratingRepository.save(rating);

        summaryRepository.updateSummary(rating.getTargetId(), rating.getTargetType(), 0, newValue - oldValue);
    }

    @Transactional(readOnly = true)
    public Integer findUserRatingValue(Long userId, String targetId, RatingTargetType type) {

        if (userId == null) {
            return null;
        }

        return ratingRepository
                .findByUserIdAndTargetIdAndTargetType(userId, targetId, type)
                .map(Rating::getRatingValue)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Double getGlobalAverageForType(RatingTargetType type) {
        return summaryRepository.getGlobalAverage(type);
    }

    @Transactional(readOnly = true)
    public List<RatingSummary> getTopSummaries(RatingTargetType type, long m, double C, int resultLimit) {
        return summaryRepository.findTopSummaries(
                type, m, C, m, PageRequest.of(0, resultLimit));
    }

    @Transactional(readOnly = true)
    public PagedResponse<RatingResponse> getCommentsPageForTarget(String targetId, RatingTargetType type, Pageable pageable) {
        Page<Rating> page = ratingRepository.findAllByTargetIdAndTargetTypeAndCommentIsNotNull(
                targetId, type, pageable);

        List<RatingResponse> content = page.getContent().stream()
                .map(RatingResponse::fromEntity)
                .toList();

        Integer nextPage = page.hasNext() ? page.getNumber() + 1 : null;

        return new PagedResponse<>(content, nextPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RatingDetailsResponse> getUserComments(Long userId, RatingTargetType filterType, Pageable pageable) {
        Page<Rating> ratingsPage = (filterType == null)
                ? ratingRepository.findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(userId, pageable)
                : ratingRepository.findAllByUserIdAndTargetTypeAndCommentIsNotNullAndCommentIsNotEmpty(userId, filterType, pageable);

        return convertToPagedResponse(ratingsPage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RatingDetailsResponse> getGlobalReviews(Pageable pageable) {
        Page<Rating> ratingsPage = ratingRepository.findByCommentIsNotNullAndCommentNotEmptyOrderByCreatedAtDesc(pageable);

        return convertToPagedResponse(ratingsPage);
    }



    private PagedResponse<RatingDetailsResponse> convertToPagedResponse(Page<Rating> ratingsPage) {
        List<Rating> ratings = ratingsPage.getContent();

        Set<String> trackIds = new HashSet<>();
        Set<String> albumIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();

        for (Rating r : ratings) {
            userIds.add(r.getUserId());
            if (r.getTargetType() == RatingTargetType.TRACK) {
                trackIds.add(r.getTargetId());
            } else if (r.getTargetType() == RatingTargetType.ALBUM) {
                albumIds.add(r.getTargetId());
            }
        }

        Map<String, TrackEntity> tracks = trackIds.isEmpty() ? Map.of() : metadataService.getOrFetchTracks(new ArrayList<>(trackIds));
        Map<String, AlbumEntity> albums = albumIds.isEmpty() ? Map.of() : metadataService.getOrFetchAlbums(new ArrayList<>(albumIds));
        Map<Long, String> usernames = userService.getUsernamesByIds(userIds);


        List<RatingDetailsResponse> details = ratings.stream()
                .map(r -> mapToDetails(r, tracks, albums, usernames))
                .toList();

        return new PagedResponse<>(details, ratingsPage.hasNext() ? ratingsPage.getNumber() + 1 : null);
    }

    private RatingDetailsResponse mapToDetails(Rating r, Map<String, TrackEntity> tracks, Map<String, AlbumEntity> albums, Map<Long, String> usernames) {
        RateableEntity entity = null;

        if (r.getTargetType() == RatingTargetType.TRACK) {
            entity = tracks.get(r.getTargetId());
        } else if (r.getTargetType() == RatingTargetType.ALBUM) {
            entity = albums.get(r.getTargetId());
        }

        String username = usernames.get(r.getUserId());

        if (entity == null) {
            return new RatingDetailsResponse(
                    r.getId(),
                    r.getTargetId(),
                    r.getTargetType(),
                    r.getRatingValue(),
                    r.getComment(),
                    r.getUserId(),
                    "User_" + r.getUserId(),
                    "Unknown",
                    null,
                    "Unknown",
                    r.getCreatedAt(),
                    r.getUpdatedAt()
            );
        }

        return RatingDetailsResponse.fromEntities(r, entity, username);
    }
}