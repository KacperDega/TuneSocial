package com.tunesocial.backend.integration.genius.adapter;

import com.tunesocial.backend.integration.genius.model.*;
import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.dto.ExternalLinkDto;
import com.tunesocial.backend.music.dto.ExternalLinkType;
import com.tunesocial.backend.music.dto.TrackResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
                formatDate(song.releaseDate()),
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
                formatDate(song.releaseDate()),
                song.primaryArtists(),
                song.featuredArtists()
        );
    }

    private TrackResponse mapToTrackResponse(
            String id, String title, String url, String imageUrl,
            String date, List<GeniusArtistRef> primary, List<GeniusArtistRef> featured) {

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

    private String formatDate(String date) {
        if (date == null || date.isBlank()) return "";

        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(date)
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        if (date.matches("\\d{4}-\\d{2}")) {
            return YearMonth.parse(date)
                    .format(DateTimeFormatter.ofPattern("MM.yyyy"));
        }

        if (date.matches("\\d{4}")) {
            return date;
        }

        return "";
    }

    private String formatDate(ReleaseDateComponents c) {
        if (c == null || c.year() == null) {
            return "";
        }

        Integer year = c.year();
        Integer month = c.month();
        Integer day = c.day();

        if (month != null && day != null) {
            return String.format("%02d.%02d.%d", day, month, year);
        } else if (month != null) {
            return String.format("%02d.%d", month, year);
        }
        return String.valueOf(year);
    }
}
