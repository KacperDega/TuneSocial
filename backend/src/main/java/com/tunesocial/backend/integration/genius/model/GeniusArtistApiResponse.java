package com.tunesocial.backend.integration.genius.model;


public record GeniusArtistApiResponse(
        ArtistGeniusResponse response
) {
    public record ArtistGeniusResponse (GeniusArtist artist) {}
}
