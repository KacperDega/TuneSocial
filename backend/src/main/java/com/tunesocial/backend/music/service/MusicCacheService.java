package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.exception.MusicItemNotFoundException;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.mapper.MusicEntityMapper;
import com.tunesocial.backend.music.repository.AlbumRepository;
import com.tunesocial.backend.music.repository.ArtistRepository;
import com.tunesocial.backend.music.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicCacheService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final MusicEntityMapper musicEntityMapper;
    private final ArtistRepository artistRepository;

    @Value("${app.cache.ttl-days}")
    private int CACHE_TTL_DAYS;

    @Transactional
    public TrackEntity cacheTrack(TrackResponse response) {
        TrackEntity entity = musicEntityMapper.toTrackEntity(response);

        if (response.album() != null) {
            AlbumEntity albumRef = albumRepository.findById(response.album().id())
                    .orElseGet(() -> createAlbumStub(response.album()));
            entity.setAlbum(albumRef);
        }

        entity.setLastUpdated(Instant.now());
        return trackRepository.save(entity);
    }

    @Transactional
    public AlbumEntity cacheAlbumWithTracks(AlbumSummaryResponse albumResp, List<TrackResponse> tracks) {
        AlbumEntity albumEntity = albumRepository.findById(albumResp.id())
                .orElseGet(() -> musicEntityMapper.toAlbumEntity(albumResp));

        musicEntityMapper.updateAlbumFromResponse(albumResp, albumEntity);
        albumEntity.setLastUpdated(Instant.now());

        for (TrackResponse trackResp : tracks) {
            TrackEntity trackEntity = trackRepository.findById(trackResp.id())
                    .orElseGet(() -> musicEntityMapper.toTrackEntity(trackResp));

            musicEntityMapper.updateTrackFromResponse(trackResp, trackEntity);
            trackEntity.setLastUpdated(Instant.now());

            albumEntity.addTrack(trackEntity);
        }

        return albumRepository.save(albumEntity);
    }

    private AlbumEntity createAlbumStub(AlbumRefDto ref) {
        AlbumEntity stub = new AlbumEntity();
        stub.setId(ref.id());
        stub.setTitle(ref.name());
        stub.setLastUpdated(Instant.now().minus(CACHE_TTL_DAYS + 1, ChronoUnit.DAYS));
        return albumRepository.save(stub);
    }

    @Transactional
    public ArtistEntity cacheArtist(ArtistResponse response) {
        ArtistEntity entity = artistRepository.findById(response.id())
                .orElseGet(() -> musicEntityMapper.toArtistEntity(response));

        musicEntityMapper.updateArtistFromResponse(response, entity);
        entity.setLastUpdated(Instant.now());

        return artistRepository.save(entity);
    }

    @Transactional
    public List<AlbumEntity> cacheDiscography(String artistId, List<AlbumSummaryResponse> discography) {
        ArtistEntity artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new MusicItemNotFoundException("Artist not found for discography update"));

        artist.setDiscographyLastUpdated(Instant.now());

        List<AlbumEntity> cachedAlbums = new ArrayList<>();

        for (AlbumSummaryResponse albumResp : discography) {
            AlbumEntity album = albumRepository.findById(albumResp.id())
                    .map(existingAlbum -> {
                        // update only metadata
                        musicEntityMapper.updateAlbumFromResponse(albumResp, existingAlbum);
                        return existingAlbum;
                    })
                    .orElseGet(() -> {
                        // add new album with only metadata
                        AlbumEntity newAlbum = musicEntityMapper.toAlbumEntity(albumResp);
                        newAlbum.setLastUpdated(Instant.now().minus(CACHE_TTL_DAYS + 1, ChronoUnit.DAYS));
                        return newAlbum;
                    });

            cachedAlbums.add(albumRepository.save(album));
        }

        return cachedAlbums;
    }

    @Transactional
    public void updateTrackMetadataIfPresent(TrackResponse searchResult) {
        Optional<TrackEntity> track = trackRepository.findById(searchResult.id());

        track.ifPresent(existingTrack -> {
            musicEntityMapper.updateTrackFromResponse(searchResult, existingTrack);
            existingTrack.setLastUpdated(Instant.now());

            trackRepository.save(existingTrack);
            log.debug("Updated existing track metadata from search: {}", searchResult.id());
        });
    }
}
