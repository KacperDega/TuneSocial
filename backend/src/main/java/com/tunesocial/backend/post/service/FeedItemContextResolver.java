package com.tunesocial.backend.post.service;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.service.MusicMetadataService;
import com.tunesocial.backend.post.model.FeedItemContext;
import com.tunesocial.backend.post.model.enums.FeedItemType;
import com.tunesocial.backend.rating.exception.RatingNotFoundException;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeedItemContextResolver {

    private final MusicMetadataService musicMetadataService;
    private final RatingService ratingService;

    public FeedItemContext createContext(FeedItemType type, String referenceId) {
        if (type == FeedItemType.TEXT_POST || referenceId == null || referenceId.isBlank()) {
            return new FeedItemContext();
        }

        return switch (type) {
            case TRACK_POST, TRACK_OF_THE_DAY -> createForTrack(referenceId);
            case ALBUM_POST, ALBUM_OF_THE_DAY -> createForAlbum(referenceId);
            case ARTIST_POST -> createForArtist(referenceId);
            case RATING_REVIEW -> createForRating(referenceId);
            default -> new FeedItemContext();
        };
    }

    private FeedItemContext createForTrack(String trackId) {
        TrackEntity track = musicMetadataService.getOrFetchTrack(trackId);
        String artists = track.getArtists().stream()
                .map(ArtistRefDto::name)
                .collect(Collectors.joining(", "));

        return new FeedItemContext(track.getTitle(), artists, track.getImageUrl());
    }

    private FeedItemContext createForAlbum(String albumId) {
        AlbumEntity album = musicMetadataService.getOrFetchAlbum(albumId);
        String artists = album.getArtists().stream()
                .map(ArtistRefDto::name)
                .collect(Collectors.joining(", "));

        return new FeedItemContext(album.getTitle(), artists, album.getImageUrl());
    }

    private FeedItemContext createForArtist(String artistId) {
        ArtistEntity artist = musicMetadataService.getOrFetchArtist(artistId);
        String subtitle = truncSubtitle(artist.getDescription());

        return new FeedItemContext(artist.getName(), subtitle, artist.getImageUrl());
    }

    private FeedItemContext createForRating(String referenceId) {
        Long ratingId = Long.parseLong(referenceId);
        Rating rating = ratingService.getRatingById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));

        String title = null;
        String imageUrl = null;

        if (rating.getTargetType() == RatingTargetType.TRACK) {
            TrackEntity track = musicMetadataService.getOrFetchTrack(rating.getTargetId());
            title = track.getTitle();
            imageUrl = track.getImageUrl();
        } else if (rating.getTargetType() == RatingTargetType.ALBUM) {
            AlbumEntity album = musicMetadataService.getOrFetchAlbum(rating.getTargetId());
            title = album.getTitle();
            imageUrl = album.getImageUrl();
        }

        String subtitle = rating.getRatingValue() + " / 10";

        return new FeedItemContext(title, subtitle, imageUrl);
    }

    private String truncSubtitle(String text) {
        int maxLength = 255;
        if (text == null || text.isBlank()) {
            return null;
        }
        text = text.trim();
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}