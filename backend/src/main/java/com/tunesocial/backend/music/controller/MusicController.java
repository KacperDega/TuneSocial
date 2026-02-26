package com.tunesocial.backend.music.controller;

import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.service.MusicService;
import com.tunesocial.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;
    private final UserService userService;

//    @GetMapping("/tracks/{trackId}")
//    public TrackResponse getTrack(@PathVariable String trackId) {
//        return musicService.getTrack(trackId);
//    }

    @GetMapping("/albums/{albumId}")
    public AlbumSummaryResponse getAlbumSummary(@PathVariable String albumId) {
        return musicService.getAlbum(albumId);
    }

    @GetMapping("/artists/{artistId}")
    public ArtistResponse getArtist(@PathVariable String artistId) {
        return musicService.getArtist(artistId);
    }

    @GetMapping("/artists/{artistId}/albums")
    public List<AlbumSummaryResponse> getDiscography(@PathVariable String artistId) {
        return musicService.getDiscography(artistId);
    }

    @GetMapping("/albums/{albumId}/tracklist")
    public List<TrackResponse> getTracklist(@PathVariable String albumId) {
        return musicService.getTracklist(albumId);
    }

    @GetMapping("/tracks/{trackId}")
    public TrackDetailsResponse getTrackDetails(@PathVariable String trackId, Authentication authentication) {
        Long userId = userService.getCurrentUserIdOrNull(authentication);

        return musicService.getTrackDetails(trackId, userId);
    }

    @GetMapping("/albums/{albumId}/details")
    public AlbumDetailsResponse getAlbumDetails(@PathVariable String albumId, Authentication authentication) {
        Long userId = userService.getCurrentUserIdOrNull(authentication);

        return musicService.getAlbumDetails(albumId, userId);
    }

}

