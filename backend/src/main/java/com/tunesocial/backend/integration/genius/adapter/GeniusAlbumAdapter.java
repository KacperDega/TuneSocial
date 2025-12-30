package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.model.GeniusAlbum;
import com.tunesocial.backend.integration.genius.model.GeniusAlbumApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusArtistRef;
import com.tunesocial.backend.integration.genius.model.ReleaseDateComponents;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistRefDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeniusAlbumAdapter {

    public AlbumSummaryResponse adapt(GeniusAlbumApiResponse res) {
        GeniusAlbum album = res.response().album();

        List<ArtistRefDto> artists = adaptArtists(album.primaryArtists());

        String releaseDate = formatReleaseDate(album.releaseDate());

        return new AlbumSummaryResponse(
                album.id(),
                album.name(),
                artists,
                album.coverArtUrl(),
                releaseDate
        );
    }

    private List<ArtistRefDto> adaptArtists(List<GeniusArtistRef> primaryArtists) {
        if (primaryArtists == null) return List.of();

        return primaryArtists.stream()
                .map(artist -> new ArtistRefDto(
                        artist.id(),
                        artist.name()
                ))
                .toList();
    }

    private String formatReleaseDate(ReleaseDateComponents components) {
        if (components == null || components.year() == null) {
            return "";
        }

        Integer year = components.year();
        Integer month = components.month();
        Integer day = components.day();

        if (month != null && day != null) {
            return String.format("%02d.%02d.%d", day, month, year);
        }

        if (month != null) {
            return String.format("%02d.%d", month, year);
        }

        return String.valueOf(year);
    }
}

