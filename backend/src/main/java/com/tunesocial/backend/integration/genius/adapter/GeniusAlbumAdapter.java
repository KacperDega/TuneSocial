package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.mapper.GeniusAlbumMapper;
import com.tunesocial.backend.integration.genius.model.GeniusAlbum;
import com.tunesocial.backend.integration.genius.model.GeniusResponses.GeniusAlbumApiResponse;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GeniusAlbumAdapter {

    private final GeniusAlbumMapper geniusAlbumMapper;

    public AlbumSummaryResponse adaptAlbum(GeniusAlbumApiResponse res) {
        GeniusAlbum album = res.response().album();

        return geniusAlbumMapper.toAlbumSummary(album);
    }
}

