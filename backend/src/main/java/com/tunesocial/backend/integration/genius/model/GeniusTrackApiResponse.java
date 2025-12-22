package com.tunesocial.backend.integration.genius.model;

public record GeniusTrackApiResponse(
        SongResponse response
) {
    public record SongResponse(GeniusSong song) {}
}

