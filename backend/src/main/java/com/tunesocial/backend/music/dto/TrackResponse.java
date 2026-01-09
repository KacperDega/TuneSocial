package com.tunesocial.backend.music.dto;

import java.util.List;

public record TrackResponse(
        String id,
        String title,
        List<ArtistRefDto> artists,
        String imageUrl,
        String releaseDate,
        List<ExternalLinkDto> links
) {
}
