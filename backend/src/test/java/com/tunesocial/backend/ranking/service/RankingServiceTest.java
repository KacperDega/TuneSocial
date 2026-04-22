package com.tunesocial.backend.ranking.service;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.service.MusicMetadataService;
import com.tunesocial.backend.ranking.dto.AlbumRankingResponse;
import com.tunesocial.backend.ranking.dto.TrackRankingResponse;
import com.tunesocial.backend.ranking.mapper.RankingMapper;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RatingService ratingService;

    @Mock
    private MusicMetadataService metadataService;

    @Mock
    private RankingMapper rankingMapper;

    @InjectMocks
    private RankingService rankingService;

    private static final long MIN_VOTES = 5L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rankingService, "m", MIN_VOTES);
    }

    @Nested
    class GetTopTracks {

        @Test
        @DisplayName("Should use global average as C when available")
        void shouldCalculateTopTracks_usingGlobalAverage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            double globalAvg = 8.2;
            String trackId = "track-123";

            RatingSummary summary = mock(RatingSummary.class);
            when(summary.getTargetId()).thenReturn(trackId);

            Page<RatingSummary> summariesPage =
                    new PageImpl<>(List.of(summary), pageable, 1);

            TrackEntity trackEntity = mock(TrackEntity.class);
            TrackRankingResponse rankingResponse = mock(TrackRankingResponse.class);

            when(ratingService.getGlobalAverageForType(RatingTargetType.TRACK))
                    .thenReturn(globalAvg);
            when(ratingService.getTopSummaries(
                    RatingTargetType.TRACK,
                    MIN_VOTES,
                    globalAvg,
                    pageable
            )).thenReturn(summariesPage);
            when(metadataService.getOrFetchTracks(List.of(trackId)))
                    .thenReturn(Map.of(trackId, trackEntity));
            when(rankingMapper.toTrackRankingResponse(trackEntity, summary))
                    .thenReturn(rankingResponse);

            // When
            PagedResponse<TrackRankingResponse> result =
                    rankingService.getTopTracks(pageable);

            // Then
            assertThat(result.content()).containsExactly(rankingResponse);
            assertThat(result.nextPage()).isNull();

            verify(ratingService).getTopSummaries(
                    RatingTargetType.TRACK,
                    MIN_VOTES,
                    8.2,
                    pageable
            );
        }

        @Test
        @DisplayName("Should fallback C parameter to 5.0 when global average is null")
        void shouldFallbackToDefaultC_whenGlobalAverageIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<RatingSummary> emptyPage =
                    new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(ratingService.getGlobalAverageForType(RatingTargetType.TRACK))
                    .thenReturn(null);
            when(ratingService.getTopSummaries(
                    RatingTargetType.TRACK,
                    MIN_VOTES,
                    5.0,
                    pageable
            )).thenReturn(emptyPage);
            when(metadataService.getOrFetchTracks(Collections.emptyList()))
                    .thenReturn(Map.of());

            // When
            PagedResponse<TrackRankingResponse> result =
                    rankingService.getTopTracks(pageable);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.nextPage()).isNull();

            verify(ratingService).getTopSummaries(
                    RatingTargetType.TRACK,
                    MIN_VOTES,
                    5.0,
                    pageable
            );
        }

        @Test
        @DisplayName("Should correctly set nextPage index when more pages are available")
        void shouldSetNextPage_whenHasNextPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 1);
            String track1 = "track-1";

            RatingSummary summary1 = mock(RatingSummary.class);
            when(summary1.getTargetId()).thenReturn(track1);

            Page<RatingSummary> summariesPage =
                    new PageImpl<>(List.of(summary1), pageable, 2);

            when(ratingService.getGlobalAverageForType(RatingTargetType.TRACK))
                    .thenReturn(6.0);
            when(ratingService.getTopSummaries(
                    RatingTargetType.TRACK,
                    MIN_VOTES,
                    6.0,
                    pageable
            )).thenReturn(summariesPage);
            when(metadataService.getOrFetchTracks(List.of(track1)))
                    .thenReturn(Map.of());

            // When
            PagedResponse<TrackRankingResponse> result =
                    rankingService.getTopTracks(pageable);

            // Then
            assertThat(result.nextPage()).isEqualTo(1);
        }
    }

    @Nested
    class GetTopAlbums {

        @Test
        @DisplayName("Should use global average as C parameter and map album metadata correctly")
        void shouldCalculateTopAlbums_successfully() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            double globalAvg = 7.0;
            String albumId = "album-999";

            RatingSummary summary = mock(RatingSummary.class);
            when(summary.getTargetId()).thenReturn(albumId);

            Page<RatingSummary> summariesPage =
                    new PageImpl<>(List.of(summary), pageable, 1);

            AlbumEntity albumEntity = mock(AlbumEntity.class);
            AlbumRankingResponse rankingResponse = mock(AlbumRankingResponse.class);

            when(ratingService.getGlobalAverageForType(RatingTargetType.ALBUM))
                    .thenReturn(globalAvg);
            when(ratingService.getTopSummaries(
                    RatingTargetType.ALBUM,
                    MIN_VOTES,
                    globalAvg,
                    pageable
            )).thenReturn(summariesPage);
            when(metadataService.getOrFetchAlbums(List.of(albumId)))
                    .thenReturn(Map.of(albumId, albumEntity));
            when(rankingMapper.toAlbumRankingResponse(albumEntity, summary))
                    .thenReturn(rankingResponse);

            // When
            PagedResponse<AlbumRankingResponse> result =
                    rankingService.getTopAlbums(pageable);

            // Then
            assertThat(result.content()).containsExactly(rankingResponse);
            assertThat(result.nextPage()).isNull();

            verify(ratingService).getTopSummaries(
                    RatingTargetType.ALBUM,
                    MIN_VOTES,
                    7.0,
                    pageable
            );
            verify(metadataService).getOrFetchAlbums(List.of(albumId));
        }
    }
}
