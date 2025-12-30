package com.tunesocial.backend.music.dto;

import java.time.LocalDate;
import java.util.List;

public record AlbumSummaryResponse(
        String id,
        String title,
        List<ArtistRefDto> artists,
        String imageUrl,
        String releaseDate
) {
}
