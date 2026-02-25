package com.tunesocial.backend.integration.genius.model.GeniusResponses;

import com.tunesocial.backend.integration.genius.model.GeniusTracklistSong;

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
