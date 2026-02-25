package com.tunesocial.backend.integration.genius.model.GeniusResponses;


import com.tunesocial.backend.integration.genius.model.GeniusArtist;

public record GeniusArtistApiResponse(
        ArtistGeniusResponse response
) {
    public record ArtistGeniusResponse (GeniusArtist artist) {}
}
