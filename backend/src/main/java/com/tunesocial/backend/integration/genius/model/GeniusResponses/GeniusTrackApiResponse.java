package com.tunesocial.backend.integration.genius.model.GeniusResponses;

import com.tunesocial.backend.integration.genius.model.GeniusSong;

public record GeniusTrackApiResponse(
        SongResponse response
) {
    public record SongResponse(GeniusSong song) {}
}

