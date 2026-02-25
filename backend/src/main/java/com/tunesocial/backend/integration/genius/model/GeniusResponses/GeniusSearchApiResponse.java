package com.tunesocial.backend.integration.genius.model.GeniusResponses;

import com.tunesocial.backend.integration.genius.model.GeniusTracklistSong;

import java.util.List;

public record GeniusSearchApiResponse(
        SearchResponse response
) {
    public record SearchResponse(
            List<Hit> hits
    ) {}

    public record Hit(
            String type,
            GeniusTracklistSong result
    ) {}
}
