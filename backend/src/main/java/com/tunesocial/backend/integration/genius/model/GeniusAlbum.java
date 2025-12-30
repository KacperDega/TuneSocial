package com.tunesocial.backend.integration.genius.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeniusAlbum(
        String id,
        String name,
        @JsonProperty("cover_art_url")
        String coverArtUrl,
        @JsonProperty("primary_artists")
        List<GeniusArtistRef> primaryArtists,
        @JsonProperty("release_date_components")
        ReleaseDateComponents releaseDate
) {
}
