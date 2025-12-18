package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicDataProvider provider;

    public TrackResponse getTrack(String id) {
        return provider.getTrack(id);
    }
}
