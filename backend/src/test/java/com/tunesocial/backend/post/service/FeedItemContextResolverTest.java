package com.tunesocial.backend.post.service;

import com.tunesocial.backend.post.model.FeedItemContext;
import com.tunesocial.backend.post.model.enums.FeedItemType;
import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.service.MusicMetadataService;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.service.RatingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedItemContextResolverTest {

    @Mock
    private MusicMetadataService musicMetadataService;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private FeedItemContextResolver feedItemContextResolver;

    @Nested
    class CreateContext {

        @Test
        @DisplayName("Should return empty context when type is TEXT_POST")
        void shouldReturnEmptyContext_whenTypeIsTextPost() {
            // Given
            FeedItemType type = FeedItemType.TEXT_POST;
            String referenceId = "track_123";

            // When
            FeedItemContext result = feedItemContextResolver.createContext(type, referenceId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isNull();
            assertThat(result.getSubtitle()).isNull();
            assertThat(result.getImageUrl()).isNull();
            verifyNoInteractions(musicMetadataService, ratingService);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        @DisplayName("Should return empty context when referenceId is null or blank")
        void shouldReturnEmptyContext_whenReferenceIdIsNullOrEmpty(String referenceId) {
            // Given
            FeedItemType type = FeedItemType.TRACK_POST;

            // When
            FeedItemContext result = feedItemContextResolver.createContext(type, referenceId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isNull();
            assertThat(result.getSubtitle()).isNull();
            assertThat(result.getImageUrl()).isNull();
            verifyNoInteractions(musicMetadataService, ratingService);
        }

        @ParameterizedTest
        @EnumSource(value = FeedItemType.class, names = {"TRACK_POST", "TRACK_OF_THE_DAY"})
        @DisplayName("Should correctly resolve context for track post types")
        void shouldResolveContext_forTrackPostTypes(FeedItemType type) {
            // Given
            String trackId = "track_1";
            TrackEntity track = mock(TrackEntity.class);
            ArtistRefDto artist1 = new ArtistRefDto("art_1", "Artist One");
            ArtistRefDto artist2 = new ArtistRefDto("art_2", "Artist Two");

            when(track.getTitle()).thenReturn("Some Track");
            when(track.getArtists()).thenReturn(List.of(artist1, artist2));
            when(track.getImageUrl()).thenReturn("someUrl.com");
            when(musicMetadataService.getOrFetchTrack(trackId)).thenReturn(track);

            // When
            FeedItemContext result = feedItemContextResolver.createContext(type, trackId);

            // Then
            assertThat(result.getTitle()).isEqualTo("Some Track");
            assertThat(result.getSubtitle()).isEqualTo("Artist One, Artist Two");
            assertThat(result.getImageUrl()).isEqualTo("someUrl.com");
            verify(musicMetadataService).getOrFetchTrack(trackId);
        }

        @ParameterizedTest
        @EnumSource(value = FeedItemType.class, names = {"ALBUM_POST", "ALBUM_OF_THE_DAY"})
        @DisplayName("Should correctly resolve context for album post types")
        void shouldResolveContext_forAlbumPostTypes(FeedItemType type) {
            // Given
            String albumId = "album_1";
            AlbumEntity album = mock(AlbumEntity.class);
            ArtistRefDto artist = new ArtistRefDto("art_1", "Artist Name");

            when(album.getTitle()).thenReturn("Some Album");
            when(album.getArtists()).thenReturn(List.of(artist));
            when(album.getImageUrl()).thenReturn("someUrl.com");
            when(musicMetadataService.getOrFetchAlbum(albumId)).thenReturn(album);

            // When
            FeedItemContext result = feedItemContextResolver.createContext(type, albumId);

            // Then
            assertThat(result.getTitle()).isEqualTo("Some Album");
            assertThat(result.getSubtitle()).isEqualTo("Artist Name");
            assertThat(result.getImageUrl()).isEqualTo("someUrl.com");
            verify(musicMetadataService).getOrFetchAlbum(albumId);
        }

        @Test
        @DisplayName("Should resolve context for ARTIST_POST with truncated description when long")
        void shouldResolveContext_forArtistPostWithTruncatedDescription() {
            // Given
            String artistId = "artist_1";
            ArtistEntity artist = mock(ArtistEntity.class);
            String longBio = "A".repeat(300);

            when(artist.getName()).thenReturn("Artist Name");
            when(artist.getDescription()).thenReturn(longBio);
            when(artist.getImageUrl()).thenReturn("someUrl.com");
            when(musicMetadataService.getOrFetchArtist(artistId)).thenReturn(artist);

            // When
            FeedItemContext result = feedItemContextResolver.createContext(FeedItemType.ARTIST_POST, artistId);

            // Then
            assertThat(result.getTitle()).isEqualTo("Artist Name");
            assertThat(result.getSubtitle()).hasSize(255);
            assertThat(result.getSubtitle()).endsWith("...");
            assertThat(result.getImageUrl()).isEqualTo("someUrl.com");
        }

        @Test
        @DisplayName("Should resolve context for ARTIST_POST with null description")
        void shouldResolveContext_forArtistPostWithNullDescription() {
            // Given
            String artistId = "artist_1";
            ArtistEntity artist = mock(ArtistEntity.class);

            when(artist.getName()).thenReturn("Famous Artist");
            when(artist.getDescription()).thenReturn(null);
            when(artist.getImageUrl()).thenReturn("someUrl.com");
            when(musicMetadataService.getOrFetchArtist(artistId)).thenReturn(artist);

            // When
            FeedItemContext result = feedItemContextResolver.createContext(FeedItemType.ARTIST_POST, artistId);

            // Then
            assertThat(result.getTitle()).isEqualTo("Famous Artist");
            assertThat(result.getSubtitle()).isNull();
            assertThat(result.getImageUrl()).isEqualTo("someUrl.com");
        }

        @Test
        @DisplayName("Should throw RatingNotFoundException when rating ID does not exist")
        void shouldThrowException_whenRatingNotFound() {
            // Given
            Long ratingId = 999L;
            when(ratingService.getRatingById(ratingId)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> feedItemContextResolver.createContext(FeedItemType.RATING_REVIEW, "999"))
                    .isInstanceOf(RatingNotFoundException.class);
            verifyNoInteractions(musicMetadataService);
        }

        @Test
        @DisplayName("Should resolve context for RATING_REVIEW targeting a track")
        void shouldResolveContext_forRatingReviewTargetingTrack() {
            // Given
            Long ratingId = 10L;
            String targetTrackId = "track_55";
            Rating rating = mock(Rating.class);
            TrackEntity track = mock(TrackEntity.class);

            when(rating.getTargetType()).thenReturn(RatingTargetType.TRACK);
            when(rating.getTargetId()).thenReturn(targetTrackId);
            when(rating.getRatingValue()).thenReturn(8);

            when(track.getTitle()).thenReturn("Rated Song");
            when(track.getImageUrl()).thenReturn("someUrl.com");

            when(ratingService.getRatingById(ratingId)).thenReturn(Optional.of(rating));
            when(musicMetadataService.getOrFetchTrack(targetTrackId)).thenReturn(track);

            // When
            FeedItemContext result = feedItemContextResolver.createContext(FeedItemType.RATING_REVIEW, "10");

            // Then
            assertThat(result.getTitle()).isEqualTo("Rated Song");
            assertThat(result.getSubtitle()).isEqualTo("8 / 10");
            assertThat(result.getImageUrl()).isEqualTo("someUrl.com");
        }

        @Test
        @DisplayName("Should resolve context for RATING_REVIEW targeting an album")
        void shouldResolveContext_forRatingReviewTargetingAlbum() {
            // Given
            Long ratingId = 11L;
            String targetAlbumId = "album_88";
            Rating rating = mock(Rating.class);
            AlbumEntity album = mock(AlbumEntity.class);

            when(rating.getTargetType()).thenReturn(RatingTargetType.ALBUM);
            when(rating.getTargetId()).thenReturn(targetAlbumId);
            when(rating.getRatingValue()).thenReturn(10);

            when(album.getTitle()).thenReturn("Album Name");
            when(album.getImageUrl()).thenReturn("someUrl.com");

            when(ratingService.getRatingById(ratingId)).thenReturn(Optional.of(rating));
            when(musicMetadataService.getOrFetchAlbum(targetAlbumId)).thenReturn(album);

            // When
            FeedItemContext result = feedItemContextResolver.createContext(FeedItemType.RATING_REVIEW, "11");

            // Then
            assertThat(result.getTitle()).isEqualTo("Album Name");
            assertThat(result.getSubtitle()).isEqualTo("10 / 10");
            assertThat(result.getImageUrl()).isEqualTo("someUrl.com");
        }
    }
}
