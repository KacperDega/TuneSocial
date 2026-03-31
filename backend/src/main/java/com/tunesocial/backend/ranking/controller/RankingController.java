package com.tunesocial.backend.ranking.controller;

import com.tunesocial.backend.common.dto.PagedResponse;
import com.tunesocial.backend.ranking.dto.AlbumRankingResponse;
import com.tunesocial.backend.ranking.dto.TrackRankingResponse;
import com.tunesocial.backend.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/tracks/top")
    public ResponseEntity<PagedResponse<TrackRankingResponse>> getTopTracks(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingService.getTopTracks(pageable));
    }

    @GetMapping("/albums/top")
    public ResponseEntity<PagedResponse<AlbumRankingResponse>> getTopAlbums(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingService.getTopAlbums(pageable));
    }
}
