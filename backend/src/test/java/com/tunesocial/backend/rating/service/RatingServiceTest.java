package com.tunesocial.backend.rating.service;

import com.tunesocial.backend.rating.dto.RateRequest;
import com.tunesocial.backend.rating.exception.InvalidRatingValueException;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.repository.RatingRepository;
import com.tunesocial.backend.rating.repository.RatingSummaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RatingSummaryRepository summaryRepository;

    @InjectMocks
    private RatingService ratingService;

    private final Long USER_ID = 1L;
    private final String TARGET_ID = "tr-1";
    private final RatingTargetType TYPE = RatingTargetType.TRACK;

    @Nested
    class Rate {

        @Test
        @DisplayName("Should create new rating and update summary when user has not rated before")
        void shouldCreateNewRating_whenNoExistingRating() {
            // Given
            int value = 8;
            RateRequest request = new RateRequest(TARGET_ID, TYPE, value, null);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TARGET_ID, TYPE))
                    .thenReturn(Optional.empty());

            when(summaryRepository.findByTargetIdAndTargetType(TARGET_ID, TYPE))
                    .thenReturn(Optional.of(new RatingSummary()));

            // When
            ratingService.rate(USER_ID, request);

            // Then
            verify(ratingRepository).save(any(Rating.class));
            verify(summaryRepository).updateSummary(TARGET_ID, TYPE, 1, value);
        }

        @Test
        @DisplayName("Should update existing rating and adjust summary difference")
        void shouldUpdateExistingRating_whenRatingExists() {
            // Given
            Rating existing = new Rating();
            existing.setValue(5);
            existing.setTargetId(TARGET_ID);
            existing.setTargetType(TYPE);

            int newValue = 9;

            RateRequest request = new RateRequest(TARGET_ID, TYPE, newValue, null);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TARGET_ID, TYPE))
                    .thenReturn(Optional.of(existing));

            when(summaryRepository.findByTargetIdAndTargetType(TARGET_ID, TYPE))
                    .thenReturn(Optional.of(new RatingSummary()));

            // When
            ratingService.rate(USER_ID, request);

            // Then
            assertThat(existing.getValue()).isEqualTo(newValue);
            verify(summaryRepository).updateSummary(TARGET_ID, TYPE, 0, newValue - 5);
        }

        @Test
        @DisplayName("Should create summary when it does not exist")
        void shouldCreateSummary_whenNotExists() {
            // Given
            RateRequest request = new RateRequest(TARGET_ID, TYPE, 7, null);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TARGET_ID, TYPE))
                    .thenReturn(Optional.empty());

            when(summaryRepository.findByTargetIdAndTargetType(TARGET_ID, TYPE))
                    .thenReturn(Optional.empty());

            // When
            ratingService.rate(USER_ID, request);

            // Then
            verify(summaryRepository).save(any(RatingSummary.class));
        }
    }

    @Nested
    class RemoveRating {

        @Test
        @DisplayName("Should throw exception when rating does not exist")
        void shouldThrowException_whenRatingNotFound() {
            // Given
            when(ratingRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    ratingService.removeRating(USER_ID, 1L)
            ).isInstanceOf(RatingNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when user tries to delete someone else's rating")
        void shouldThrowException_whenUserNotOwner() {
            // Given
            Rating rating = new Rating();
            rating.setUserId(999L);

            when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));

            // When & Then
            assertThatThrownBy(() ->
                    ratingService.removeRating(USER_ID, 1L)
            ).isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Should delete rating and decrease summary count and sum")
        void shouldDeleteRating_andUpdateSummary() {
            // Given
            Rating rating = new Rating();
            rating.setId(1L);
            rating.setUserId(USER_ID);
            rating.setTargetId(TARGET_ID);
            rating.setTargetType(TYPE);
            rating.setValue(6);

            RatingSummary summary = new RatingSummary();
            summary.setTargetId(TARGET_ID);
            summary.setTargetType(TYPE);

            when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));
            when(summaryRepository.findByTargetIdAndTargetType(TARGET_ID, TYPE))
                    .thenReturn(Optional.of(summary));

            // When
            ratingService.removeRating(USER_ID, 1L);

            // Then
            verify(summaryRepository).updateSummary(TARGET_ID, TYPE, -1, -6);
            verify(ratingRepository).delete(rating);
        }
    }

    @Nested
    class GetSummaryForTarget {

        @Test
        @DisplayName("Should return existing summary when found")
        void shouldReturnExistingSummary() {
            // Given
            RatingSummary summary = new RatingSummary();

            when(summaryRepository.findByTargetIdAndTargetType(TARGET_ID, TYPE))
                    .thenReturn(Optional.of(summary));

            // When
            RatingSummary result = ratingService.getSummaryForTarget(TARGET_ID, TYPE);

            // Then
            assertThat(result).isSameAs(summary);
        }

        @Test
        @DisplayName("Should return empty summary when none exists")
        void shouldReturnEmptySummary_whenNotExists() {
            // Given
            when(summaryRepository.findByTargetIdAndTargetType(TARGET_ID, TYPE))
                    .thenReturn(Optional.empty());

            // When
            RatingSummary result = ratingService.getSummaryForTarget(TARGET_ID, TYPE);

            // Then
            assertThat(result.getRatingCount()).isZero();
            assertThat(result.getRatingSum()).isZero();
            assertThat(result.getTargetId()).isEqualTo(TARGET_ID);
            assertThat(result.getTargetType()).isEqualTo(TYPE);
        }
    }

    @Nested
    class FindUserRatingValue {

        @Test
        @DisplayName("Should return null when userId is null")
        void shouldReturnNull_whenUserIdNull() {
            // When
            Integer result = ratingService.findUserRatingValue(null, TARGET_ID, TYPE);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return rating value when rating exists")
        void shouldReturnValue_whenRatingExists() {
            // Given
            Rating rating = new Rating();
            rating.setValue(7);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TARGET_ID, TYPE))
                    .thenReturn(Optional.of(rating));

            // When
            Integer result = ratingService.findUserRatingValue(USER_ID, TARGET_ID, TYPE);

            // Then
            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("Should return null when rating does not exist")
        void shouldReturnNull_whenRatingNotExists() {
            // Given
            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TARGET_ID, TYPE))
                    .thenReturn(Optional.empty());

            // When
            Integer result = ratingService.findUserRatingValue(USER_ID, TARGET_ID, TYPE);

            // Then
            assertThat(result).isNull();
        }
    }
}