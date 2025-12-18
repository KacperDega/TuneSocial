package com.tunesocial.backend.music.dto;

import java.time.LocalDate;
import java.util.List;

public record TrackResponse(
        String id,
        String title,
        List<ArtistRefDto> artists,
        String imageUrl,
        LocalDate releaseDate,
        List<ExternalLinkDto> links
) {
}
