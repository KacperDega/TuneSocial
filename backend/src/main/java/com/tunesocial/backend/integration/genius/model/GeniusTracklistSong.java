package com.tunesocial.backend.integration.genius.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GeniusTracklistSong(
        String id,
        String title,
        String url,
        @JsonProperty("song_art_image_url") String songArtImageUrl,
        @JsonProperty("release_date_components") ReleaseDateComponents releaseDate,
        @JsonProperty("primary_artists") List<GeniusArtistRef> primaryArtists,
        @JsonProperty("featured_artists") List<GeniusArtistRef> featuredArtists
) {
}
