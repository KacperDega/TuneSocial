package com.tunesocial.backend.music.controller;

import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @GetMapping("/{id}")
    public TrackResponse getTrack(@PathVariable String id) {
        return musicService.getTrack(id);
    }
}

