package com.tunesocial.backend.ranking.service;

import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.service.MusicMetadataService;
import com.tunesocial.backend.ranking.dto.AlbumRankingResponse;
import com.tunesocial.backend.ranking.dto.TrackRankingResponse;
import com.tunesocial.backend.ranking.mapper.RankingMapper;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RatingService ratingService;
    private final MusicMetadataService metadataService;
    private final RankingMapper  rankingMapper;

    @Value("${app.ranking.min-votes:5}")
    private long m;

    public List<TrackRankingResponse> getTopTracks(int limit) {
        // TODO: caching to avoid constantly calculating avg
        Double globalAvg = ratingService.getGlobalAverageForType(RatingTargetType.TRACK);
        double C = (globalAvg != null) ? globalAvg : 5.0;

        List<RatingSummary> topSummaries = ratingService.getTopSummaries(
                RatingTargetType.TRACK, m, C, limit);

        List<String> ids = topSummaries.stream().map(RatingSummary::getTargetId).toList();
        Map<String, TrackEntity> metadataMap = metadataService.getOrFetchTracks(ids);

        return topSummaries.stream()
                .map(s -> rankingMapper.toTrackRankingResponse(metadataMap.get(s.getTargetId()), s))
                .toList();
    }

    public List<AlbumRankingResponse> getTopAlbums(int limit) {
        Double globalAvg = ratingService.getGlobalAverageForType(RatingTargetType.ALBUM);
        double C = (globalAvg != null) ? globalAvg : 5.0;

        List<RatingSummary> topSummaries = ratingService.getTopSummaries(
                RatingTargetType.ALBUM, m, C, limit);

        List<String> ids = topSummaries.stream().map(RatingSummary::getTargetId).toList();
        Map<String, AlbumEntity> metadataMap = metadataService.getOrFetchAlbums(ids);

        return topSummaries.stream()
                .map(s -> rankingMapper.toAlbumRankingResponse(metadataMap.get(s.getTargetId()), s))
                .toList();
    }
}
