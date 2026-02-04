package com.tunesocial.backend.music.dto;

import jakarta.persistence.ElementCollection;

import java.time.LocalDate;
import java.util.List;

public record AlbumSummaryResponse(
        String id,
        String title,

        @ElementCollection()
        List<ArtistRefDto> artists,

        String imageUrl,
        String releaseDate
) {
}
