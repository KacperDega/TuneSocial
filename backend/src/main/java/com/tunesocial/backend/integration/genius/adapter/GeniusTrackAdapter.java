package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.model.*;
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

        return mapToTrackResponse(
                song.id(),
                song.title(),
                song.url(),
                song.songArtImageUrl(),
                parseDate(song.releaseDate()),
                song.primaryArtists(),
                song.featuredArtists()
        );
    }

    public List<TrackResponse> adapt(GeniusTracklistApiResponse res) {
        if (res == null || res.response() == null || res.response().tracks() == null) {
            return List.of();
        }

        return res.response().tracks().stream()
                .map(track -> adapt(track.song()))
                .toList();
    }

    private TrackResponse adapt(GeniusTracklistSong song) {
        return mapToTrackResponse(
                song.id(),
                song.title(),
                song.url(),
                song.songArtImageUrl(),
                parseComponents(song.releaseDate()),
                song.primaryArtists(),
                song.featuredArtists()
        );
    }

    private TrackResponse mapToTrackResponse(
            String id, String title, String url, String imageUrl,
            LocalDate date, List<GeniusArtistRef> primary, List<GeniusArtistRef> featured) {

        List<ArtistRefDto> artists = new ArrayList<>(
                primary.stream()
                        .map(a -> new ArtistRefDto(a.id(), a.name()))
                        .toList()
        );

        artists.addAll(
                featured.stream()
                        .map(a -> new ArtistRefDto(a.id(), a.name()))
                        .toList()
        );

        List<ExternalLinkDto> links = List.of(
                new ExternalLinkDto(ExternalLinkType.GENIUS, url)
        );

        return new TrackResponse(id, title, artists, imageUrl, date, links);
    }

    private LocalDate parseDate(String date) {
        return (date == null) ? null : LocalDate.parse(date);
    }

    private LocalDate parseComponents(ReleaseDateComponents c) {
        if (c == null || c.year() == null) return null;

        // null
        int month = (c.month() != null) ? c.month() : 1;
        int day = (c.day() != null) ? c.day() : 1;

        return LocalDate.of(c.year(), month, day);
    }
}

