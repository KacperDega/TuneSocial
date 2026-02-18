package com.tunesocial.backend.ranking.dto;

import com.tunesocial.backend.music.dto.AlbumRefDto;
import com.tunesocial.backend.music.dto.ArtistRefDto;

import java.util.List;

public record TrackRankingResponse(
        String id,
        String title,
        String imageUrl,
        AlbumRefDto album,
        List<ArtistRefDto> artists,
        double averageRating,
        long ratingCount
) {}
