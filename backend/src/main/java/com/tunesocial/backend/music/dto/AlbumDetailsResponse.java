package com.tunesocial.backend.music.dto;

import java.util.List;

public record AlbumDetailsResponse(
        AlbumSummaryResponse album,
        List<TrackResponse> tracks
) {
}
