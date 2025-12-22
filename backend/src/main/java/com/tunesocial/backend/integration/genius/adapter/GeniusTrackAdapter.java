package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.model.GeniusSong;
import com.tunesocial.backend.integration.genius.model.GeniusTrackApiResponse;
import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.dto.ExternalLinkDto;
import com.tunesocial.backend.music.dto.ExternalLinkType;
import com.tunesocial.backend.music.dto.TrackResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class GeniusTrackAdapter {

    public TrackResponse adapt(GeniusTrackApiResponse res) {

        GeniusSong song = res.response().song();

        //
        List<ArtistRefDto> artists = new ArrayList<>(
                song.primaryArtists()
                        .stream()
                        .map(a -> new ArtistRefDto(a.id().toString(), a.name()))
                        .toList()
        );

        artists.addAll(
                song.featuredArtists()
                        .stream()
                        .map(a -> new ArtistRefDto(a.id().toString(), a.name()))
                        .toList()
        );
        //

        List<ExternalLinkDto> links = List.of(
                new ExternalLinkDto(
                        ExternalLinkType.GENIUS,
                        song.url()
                )
        );

        return new TrackResponse(
                song.id().toString(),
                song.title(),
                artists,
                song.songArtImageUrl(),
                parseDate(song.releaseDate()),
                links
        );
    }

    private LocalDate parseDate(String date) {
        if (date == null) return null;
        return LocalDate.parse(date);
    }
}


