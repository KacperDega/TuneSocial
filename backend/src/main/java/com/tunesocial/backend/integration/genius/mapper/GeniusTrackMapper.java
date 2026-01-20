package com.tunesocial.backend.integration.genius.mapper;

import com.tunesocial.backend.integration.genius.model.*;
import com.tunesocial.backend.music.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class GeniusTrackMapper {

    public TrackResponse toTrackResponse(GeniusSong song) {
        return mapToTrackResponse(
                song.id(),
                song.title(),
                song.url(),
                song.songArtImageUrl(),
                song.album(),
                formatDate(song.releaseDate()),
                song.primaryArtists(),
                song.featuredArtists()
        );
    }

    public TrackResponse toTrackResponse(GeniusTracklistSong song) {
        return mapToTrackResponse(
                song.id(),
                song.title(),
                song.url(),
                song.songArtImageUrl(),
                null,
                formatDate(song.releaseDate()),
                song.primaryArtists(),
                song.featuredArtists()
        );
    }

    private TrackResponse mapToTrackResponse(
            String id, String title, String url, String imageUrl, GeniusAlbumRef album,
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

        AlbumRefDto albumOrigin = null;
        if (album != null) {
            albumOrigin = new AlbumRefDto(album.id(), album.name());
        }

        return new TrackResponse(id, title, imageUrl, albumOrigin, date, artists, links);
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
