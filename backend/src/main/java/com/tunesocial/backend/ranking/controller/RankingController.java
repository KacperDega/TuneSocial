package com.tunesocial.backend.ranking.controller;

import com.tunesocial.backend.ranking.dto.AlbumRankingResponse;
import com.tunesocial.backend.ranking.dto.TrackRankingResponse;
import com.tunesocial.backend.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    //todo: paging
    @GetMapping("/tracks/top")
    public ResponseEntity<List<TrackRankingResponse>> getTopTracks(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TrackRankingResponse> topTracks = rankingService.getTopTracks(limit);
        return ResponseEntity.ok(topTracks);
    }

    @GetMapping("/albums/top")
    public ResponseEntity<List<AlbumRankingResponse>> getTopAlbums(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<AlbumRankingResponse> topAlbums = rankingService.getTopAlbums(limit);
        return ResponseEntity.ok(topAlbums);
    }
}
