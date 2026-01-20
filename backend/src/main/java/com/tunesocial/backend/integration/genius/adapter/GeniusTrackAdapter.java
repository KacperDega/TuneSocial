package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.mapper.GeniusTrackMapper;
import com.tunesocial.backend.integration.genius.model.*;
import com.tunesocial.backend.music.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class GeniusTrackAdapter {

    private final GeniusTrackMapper geniusTrackMapper;

    public TrackResponse adaptTrack(GeniusTrackApiResponse res) {
        GeniusSong song = res.response().song();

        return geniusTrackMapper.toTrackResponse(song);
    }

    public List<TrackResponse> adaptTrack(GeniusTracklistApiResponse res) {
        if (res == null || res.response() == null || res.response().tracks() == null) {
            return List.of();
        }

        return res.response().tracks().stream()
                .map(track -> geniusTrackMapper.toTrackResponse(track.song()))
                .toList();
    }
}
