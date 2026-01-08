package com.tunesocial.backend.integration.genius.model;

import java.util.List;

public record GeniusTracklistApiResponse(
        TracklistGeniusResponse response
) {

    public record TracklistGeniusResponse(
            List<Track> tracks
    ) {}

    public record Track(
            Integer number,
            GeniusTracklistSong song
    ) {}
}
