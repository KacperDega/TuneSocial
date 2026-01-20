package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.mapper.GeniusAlbumMapper;
import com.tunesocial.backend.integration.genius.mapper.GeniusArtistMapper;
import com.tunesocial.backend.integration.genius.model.*;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class GeniusArtistAdapter {

    private final GeniusAlbumMapper geniusAlbumMapper;
    private final GeniusArtistMapper geniusArtistMapper;

    public ArtistResponse adaptArtist(GeniusArtistApiResponse res) {
        GeniusArtist artist = res.response().artist();

        return geniusArtistMapper.toArtistResponse(artist);
    }

    public List<AlbumSummaryResponse> adaptDiscography(GeniusDiscographyApiResponse res) {
        List<GeniusAlbum> albums = res.response().albums();

        return albums.stream()
                .map(geniusAlbumMapper::toAlbumSummary)
                .collect(Collectors.toList());
    }
}
