package com.tunesocial.backend.ranking.dto;

import com.tunesocial.backend.music.dto.ArtistRefDto;

import java.util.List;

public record AlbumRankingResponse(
        String id,
        String title,
        String imageUrl,
        List<ArtistRefDto> artists,
        String releaseDate,
        double averageRating,
        long ratingCount
) {}
