package com.tunesocial.backend.rating.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.service.MusicMetadataService;
import com.tunesocial.backend.rating.dto.RateRequest;
import com.tunesocial.backend.rating.dto.RatingDetailsResponse;
import com.tunesocial.backend.rating.dto.RatingResponse;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.repository.RatingRepository;
import com.tunesocial.backend.rating.repository.RatingSummaryRepository;
import com.tunesocial.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
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

    @Mock
    private MusicMetadataService metadataService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RatingService ratingService;

    private final Long USER_ID = 1L;
    private final String TRACK_ID = "tr-1";
    private final String ALBUM_ID = "al-1";
    private final RatingTargetType TYPE = RatingTargetType.TRACK;
    private final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Nested
    class Rate {

        @Test
        @DisplayName("Should create new rating and update summary when user has not rated before")
        void shouldCreateNewRating_whenNoExistingRating() {
            // Given
            int value = 8;
            RateRequest request = new RateRequest(TRACK_ID, TYPE, value, null);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TRACK_ID, TYPE))
                    .thenReturn(Optional.empty());

            when(summaryRepository.findByTargetIdAndTargetType(TRACK_ID, TYPE))
                    .thenReturn(Optional.of(new RatingSummary()));

            // When
            ratingService.rate(USER_ID, request);

            // Then
            verify(ratingRepository).save(any(Rating.class));
            verify(summaryRepository).updateSummary(TRACK_ID, TYPE, 1, value);
        }

        @Test
        @DisplayName("Should update existing rating and adjust summary difference")
        void shouldUpdateExistingRating_whenRatingExists() {
            // Given
            Rating existing = new Rating();
            existing.setRatingValue(5);
            existing.setTargetId(TRACK_ID);
            existing.setTargetType(TYPE);

            int newValue = 9;

            RateRequest request = new RateRequest(TRACK_ID, TYPE, newValue, null);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TRACK_ID, TYPE))
                    .thenReturn(Optional.of(existing));

            when(summaryRepository.findByTargetIdAndTargetType(TRACK_ID, TYPE))
                    .thenReturn(Optional.of(new RatingSummary()));

            // When
            ratingService.rate(USER_ID, request);

            // Then
            assertThat(existing.getRatingValue()).isEqualTo(newValue);
            verify(summaryRepository).updateSummary(TRACK_ID, TYPE, 0, newValue - 5);
        }

        @Test
        @DisplayName("Should create summary when it does not exist")
        void shouldCreateSummary_whenNotExists() {
            // Given
            RateRequest request = new RateRequest(TRACK_ID, TYPE, 7, null);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TRACK_ID, TYPE))
                    .thenReturn(Optional.empty());

            when(summaryRepository.findByTargetIdAndTargetType(TRACK_ID, TYPE))
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
            rating.setTargetId(TRACK_ID);
            rating.setTargetType(TYPE);
            rating.setRatingValue(6);

            RatingSummary summary = new RatingSummary();
            summary.setTargetId(TRACK_ID);
            summary.setTargetType(TYPE);

            when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));
            when(summaryRepository.findByTargetIdAndTargetType(TRACK_ID, TYPE))
                    .thenReturn(Optional.of(summary));

            // When
            ratingService.removeRating(USER_ID, 1L);

            // Then
            verify(summaryRepository).updateSummary(TRACK_ID, TYPE, -1, -6);
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

            when(summaryRepository.findByTargetIdAndTargetType(TRACK_ID, TYPE))
                    .thenReturn(Optional.of(summary));

            // When
            RatingSummary result = ratingService.getSummaryForTarget(TRACK_ID, TYPE);

            // Then
            assertThat(result).isSameAs(summary);
        }

        @Test
        @DisplayName("Should return empty summary when none exists")
        void shouldReturnEmptySummary_whenNotExists() {
            // Given
            when(summaryRepository.findByTargetIdAndTargetType(TRACK_ID, TYPE))
                    .thenReturn(Optional.empty());

            // When
            RatingSummary result = ratingService.getSummaryForTarget(TRACK_ID, TYPE);

            // Then
            assertThat(result.getRatingCount()).isZero();
            assertThat(result.getRatingSum()).isZero();
            assertThat(result.getTargetId()).isEqualTo(TRACK_ID);
            assertThat(result.getTargetType()).isEqualTo(TYPE);
        }
    }

    @Nested
    class FindUserRatingValue {

        @Test
        @DisplayName("Should return null when userId is null")
        void shouldReturnNull_whenUserIdNull() {
            // When
            Integer result = ratingService.findUserRatingValue(null, TRACK_ID, TYPE);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return rating value when rating exists")
        void shouldReturnValue_whenRatingExists() {
            // Given
            Rating rating = new Rating();
            rating.setRatingValue(7);

            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TRACK_ID, TYPE))
                    .thenReturn(Optional.of(rating));

            // When
            Integer result = ratingService.findUserRatingValue(USER_ID, TRACK_ID, TYPE);

            // Then
            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("Should return null when rating does not exist")
        void shouldReturnNull_whenRatingNotExists() {
            // Given
            when(ratingRepository.findByUserIdAndTargetIdAndTargetType(USER_ID, TRACK_ID, TYPE))
                    .thenReturn(Optional.empty());

            // When
            Integer result = ratingService.findUserRatingValue(USER_ID, TRACK_ID, TYPE);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    class GetCommentsPageForTarget {

        @Test
        @DisplayName("Should return paged response with next page index when more results exist")
        void shouldReturnPagedResponse_WithNextPage_WhenMoreResultsExist() {
            // Given
            Rating rating = new Rating();
            rating.setId(1L);
            rating.setComment("Comment");

            Page<Rating> page = new PageImpl<>(List.of(rating), PAGEABLE, 20);

            when(ratingRepository.findAllByTargetIdAndTargetTypeAndCommentIsNotNull(TRACK_ID, RatingTargetType.TRACK, PAGEABLE))
                    .thenReturn(page);

            // When
            PagedResponse<RatingResponse> result = ratingService.getCommentsPageForTarget(TRACK_ID, RatingTargetType.TRACK, PAGEABLE);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.nextPage()).isEqualTo(1);
            assertThat(result.content().get(0).comment()).isEqualTo("Comment");
        }

        @Test
        @DisplayName("Should return null as next page when it is the last page")
        void shouldReturnNullNextPage_WhenLastPage() {
            // Given
            Page<Rating> page = new PageImpl<>(List.of(new Rating()), PAGEABLE, 1);
            when(ratingRepository.findAllByTargetIdAndTargetTypeAndCommentIsNotNull(TRACK_ID, RatingTargetType.TRACK, PAGEABLE))
                    .thenReturn(page);

            // When
            PagedResponse<RatingResponse> result = ratingService.getCommentsPageForTarget(TRACK_ID, RatingTargetType.TRACK, PAGEABLE);

            // Then
            assertThat(result.nextPage()).isNull();
        }
    }

    @Nested
    class GetUserComments {

        @Test
        @DisplayName("Should fetch track and album metadata separately and map to details")
        void shouldFetchMetadataAndMapToDetails() {
            // Given
            Rating trackRating = new Rating();
            trackRating.setTargetId(TRACK_ID);
            trackRating.setUserId(USER_ID);
            trackRating.setTargetType(RatingTargetType.TRACK);

            Rating albumRating = new Rating();
            albumRating.setTargetId(ALBUM_ID);
            albumRating.setUserId(USER_ID);
            albumRating.setTargetType(RatingTargetType.ALBUM);

            Page<Rating> page = new PageImpl<>(List.of(trackRating, albumRating));

            when(ratingRepository.findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(USER_ID, PAGEABLE))
                    .thenReturn(page);

            TrackEntity trackEntity = new TrackEntity();
            trackEntity.setArtists(List.of(new ArtistRefDto("1", "TrackArtist")));
            AlbumEntity albumEntity = new AlbumEntity();
            albumEntity.setArtists(List.of(new ArtistRefDto("2", "AlbumArtist")));

            when(metadataService.getOrFetchTracks(List.of(TRACK_ID))).thenReturn(Map.of(TRACK_ID, trackEntity));
            when(metadataService.getOrFetchAlbums(List.of(ALBUM_ID))).thenReturn(Map.of(ALBUM_ID, albumEntity));
            when(userService.getUsernamesByIds(anySet())).thenReturn(Map.of(USER_ID, "TestUser"));

            // When
            PagedResponse<RatingDetailsResponse> result = ratingService.getUserComments(USER_ID, null, PAGEABLE);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).username()).isEqualTo("TestUser");
            verify(userService).getUsernamesByIds(anySet());
            verify(metadataService).getOrFetchTracks(anyList());
            verify(metadataService).getOrFetchAlbums(anyList());
        }

        @Test
        @DisplayName("Should return 'Unknown' data when metadata is missing in the service")
        void shouldReturnUnknown_WhenMetadataNotFound() {
            // Given
            Rating rating = new Rating();
            rating.setTargetId(TRACK_ID);
            rating.setTargetType(RatingTargetType.TRACK);

            when(ratingRepository.findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(USER_ID, PAGEABLE))
                    .thenReturn(new PageImpl<>(List.of(rating)));

            when(metadataService.getOrFetchTracks(anyList())).thenReturn(Map.of());

            // When
            PagedResponse<RatingDetailsResponse> result = ratingService.getUserComments(USER_ID, null, PAGEABLE);

            // Then
            RatingDetailsResponse details = result.content().get(0);
            assertThat(details.title()).isEqualTo("Unknown");
            assertThat(details.authorName()).isEqualTo("Unknown");
            assertThat(details.imageUrl()).isNull();
        }

        @Test
        @DisplayName("Should use filtered repository method when filterType is given")
        void shouldCallFilteredRepository_WhenTypeIsGiven() {
            // Given
            RatingTargetType filter = RatingTargetType.ALBUM;
            when(ratingRepository.findAllByUserIdAndTargetTypeAndCommentIsNotNullAndCommentIsNotEmpty(USER_ID, filter, PAGEABLE))
                    .thenReturn(new PageImpl<>(List.of()));

            // When
            ratingService.getUserComments(USER_ID, filter, PAGEABLE);

            // Then
            verify(ratingRepository).findAllByUserIdAndTargetTypeAndCommentIsNotNullAndCommentIsNotEmpty(USER_ID, filter, PAGEABLE);
            verify(ratingRepository, never()).findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(anyLong(), any());
        }

        @Test
        @DisplayName("Should fallback to 'User_ID' when username is missing in UserService")
        void shouldFallbackToUserId_WhenUsernameNotFound() {
            // Given
            Rating rating = new Rating();
            rating.setUserId(999L);
            rating.setTargetId(TRACK_ID);
            rating.setTargetType(RatingTargetType.TRACK);

            when(ratingRepository.findAllByUserIdAndCommentIsNotNullAndCommentIsNotEmpty(anyLong(), any()))
                    .thenReturn(new PageImpl<>(List.of(rating)));

            when(userService.getUsernamesByIds(anySet())).thenReturn(Map.of());

            TrackEntity track = new TrackEntity();
            track.setArtists(List.of());
            when(metadataService.getOrFetchTracks(anyList())).thenReturn(Map.of(TRACK_ID, track));

            // When
            PagedResponse<RatingDetailsResponse> result = ratingService.getUserComments(USER_ID, null, PAGEABLE);

            // Then
            assertThat(result.content().get(0).username()).isEqualTo("User_999");
        }
    }
}