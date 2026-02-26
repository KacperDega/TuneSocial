package com.tunesocial.backend.rating.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.rating.dto.RateRequest;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.exception.InvalidRatingValueException;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.exception.RatingSummaryNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.repository.RatingRepository;
import com.tunesocial.backend.rating.repository.RatingSummaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RatingSummaryRepository summaryRepository;


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

        summaryRepository.updateSummary(summary.getTargetId(), summary.getTargetType(), -1, -rating.getValue());
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
        rating.setValue(request.value());
        rating.setComment(request.comment());

        rating.setCreatedAt(Instant.now());
        rating.setUpdatedAt(rating.getCreatedAt());

        ratingRepository.save(rating);

        summaryRepository.updateSummary(request.targetId(), request.targetType(), 1, request.value());
    }

    private void updateExistingRating(Rating rating, int newValue, String comment) {
        int oldValue = rating.getValue();
        rating.setValue(newValue);

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
                .map(Rating::getValue)
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
    public PagedResponse<RatingResponse> getUserComments(Long userId, Pageable pageable) {
        Page<Rating> page = ratingRepository.findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(userId, pageable);

        List<RatingResponse> content = page.getContent().stream()
                .map(RatingResponse::fromEntity)
                .toList();

        Integer nextPage = page.hasNext() ? page.getNumber() + 1 : null;

        return new PagedResponse<>(content, nextPage);
    }
}