package com.tunesocial.backend.rating.service;

import com.tunesocial.backend.rating.exception.InvalidRatingValueException;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.exception.RatingSummaryNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.repository.RatingRepository;
import com.tunesocial.backend.rating.repository.RatingSummaryRepository;
import org.springframework.data.domain.PageRequest;
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
    public void rate(Long userId, String targetId, RatingTargetType type, int value) {
        if (value < 1 || value > 10) {
            throw new InvalidRatingValueException(value);
        }

        Optional<Rating> existingOpt =
                ratingRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, type);

        summaryRepository
                .findByTargetIdAndTargetType(targetId, type)
                .orElseGet(() -> createSummary(targetId, type));

        if (existingOpt.isPresent()) {
            updateExistingRating(existingOpt.get(), value);
        } else {
            createNewRating(userId, targetId, type, value);
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

    private void createNewRating(Long userId, String targetId, RatingTargetType type, int value) {
        Rating rating = new Rating();
        rating.setUserId(userId);
        rating.setTargetId(targetId);
        rating.setTargetType(type);
        rating.setValue(value);
        rating.setCreatedAt(Instant.now());

        ratingRepository.save(rating);

        summaryRepository.updateSummary(targetId, type, 1, value);
    }

    private void updateExistingRating(Rating rating, int newValue) {
        int oldValue = rating.getValue();
        rating.setValue(newValue);

        ratingRepository.save(rating);

        summaryRepository.updateSummary(rating.getTargetId(), rating.getTargetType(), 0, newValue - oldValue);
    }

    public Integer findUserRatingValue(Long userId, String targetId, RatingTargetType type) {

        if (userId == null) {
            return null;
        }

        return ratingRepository
                .findByUserIdAndTargetIdAndTargetType(userId, targetId, type)
                .map(Rating::getValue)
                .orElse(null);
    }

    public Double getGlobalAverageForType(RatingTargetType type) {
        return summaryRepository.getGlobalAverage(type);
    }

    public List<RatingSummary> getTopSummaries(RatingTargetType type, long m, double C, int resultLimit) {
        return summaryRepository.findTopSummaries(
                type, m, C, m, PageRequest.of(0, resultLimit));
    }
}