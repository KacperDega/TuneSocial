package com.tunesocial.backend.ranking.mapper;

import com.tunesocial.backend.music.dto.AlbumRefDto;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.ranking.dto.AlbumRankingResponse;
import com.tunesocial.backend.ranking.dto.TrackRankingResponse;
import com.tunesocial.backend.rating.model.RatingSummary;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

    public TrackRankingResponse toTrackRankingResponse(TrackEntity entity, RatingSummary summary) {
        double average = summary.getRatingCount() == 0
                ? 0.0
                : (double) summary.getRatingSum() / summary.getRatingCount();

        return new TrackRankingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                new AlbumRefDto(entity.getAlbum().getId(), entity.getAlbum().getTitle()),
                entity.getArtists(),
                average,
                summary.getRatingCount()
        );
    }

    public AlbumRankingResponse toAlbumRankingResponse(AlbumEntity entity, RatingSummary summary) {
        double average = summary.getRatingCount() == 0
                ? 0.0
                : (double) summary.getRatingSum() / summary.getRatingCount();

        return new AlbumRankingResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getArtists(),
                entity.getReleaseDate(),
                average,
                summary.getRatingCount()
        );
    }
}
