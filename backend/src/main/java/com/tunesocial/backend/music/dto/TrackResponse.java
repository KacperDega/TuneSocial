package com.tunesocial.backend.music.dto;

import java.util.List;

public record TrackResponse(
        String id,
        String title,
        String imageUrl,
        AlbumRefDto album,
        String releaseDate,
        List<ArtistRefDto> artists,
        List<ExternalLinkDto> links
) {
}
