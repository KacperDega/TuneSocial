package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.dto.SearchTrackResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final MusicDataProvider musicDataProvider;
    private final MusicCacheService musicCacheService;

    public List<SearchTrackResponse> search(String query) {
        log.info("Searching for tracks with query: `{}`", query);

        List<TrackResponse> searchResults = musicDataProvider.searchTracks(query);

        searchResults.forEach(musicCacheService::updateTrackMetadataIfPresent);

        return searchResults.stream()
                .map(this::toSearchResponse)
                .toList();
    }

    private SearchTrackResponse toSearchResponse(TrackResponse track) {
        String artists;

        if (track.artists() == null || track.artists().isEmpty()) {
            artists = "Unknown Artist";
        } else {
            artists = track.artists().stream()
                    .map(ArtistRefDto::name)
                    .collect(Collectors.joining(", "));
        }

        return new SearchTrackResponse(
                track.id(),
                track.title(),
                artists,
                track.imageUrl()
        );
    }
}
