package com.tunesocial.backend.music.controller;

import com.tunesocial.backend.music.dto.SearchTrackResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Validated
public class MusicSearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchTrackResponse>> search(@RequestParam @NotBlank String query) {

        List<SearchTrackResponse> results = searchService.search(query);
        return ResponseEntity.ok(results);
    }
}
