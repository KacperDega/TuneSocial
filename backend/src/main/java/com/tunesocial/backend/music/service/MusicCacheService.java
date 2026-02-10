package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.mapper.MetadataMapper;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import com.tunesocial.backend.music.repository.AlbumRepository;
import com.tunesocial.backend.music.repository.ArtistRepository;
import com.tunesocial.backend.music.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicCacheService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final MetadataMapper metadataMapper;
    private final ArtistRepository artistRepository;

    @Value("${app.cache.ttl-days}")
    private int CACHE_TTL_DAYS;

    @Transactional
    public TrackEntity cacheTrack(TrackResponse response) {
        TrackEntity entity = metadataMapper.toEntity(response);

        if (response.album() != null) {
            AlbumEntity albumRef = albumRepository.findById(response.album().id())
                    .orElseGet(() -> createAlbumStub(response.album()));
            entity.setAlbum(albumRef);
        }

        entity.setLastUpdated(LocalDateTime.now());
        return trackRepository.save(entity);
    }

    @Transactional
    public AlbumEntity cacheAlbumWithTracks(AlbumSummaryResponse albumResp, List<TrackResponse> tracks) {
        AlbumEntity albumEntity = albumRepository.findById(albumResp.id())
                .orElseGet(() -> metadataMapper.toEntity(albumResp));

        metadataMapper.updateAlbumFromResponse(albumResp, albumEntity);
        albumEntity.setLastUpdated(LocalDateTime.now());

        for (TrackResponse trackResp : tracks) {
            TrackEntity trackEntity = trackRepository.findById(trackResp.id())
                    .orElseGet(() -> metadataMapper.toEntity(trackResp));

            metadataMapper.updateTrackFromResponse(trackResp, trackEntity);
            trackEntity.setLastUpdated(LocalDateTime.now());

            albumEntity.addTrack(trackEntity);
        }

        return albumRepository.save(albumEntity);
    }

    private AlbumEntity createAlbumStub(AlbumRefDto ref) {
        AlbumEntity stub = new AlbumEntity();
        stub.setId(ref.id());
        stub.setTitle(ref.name());
        stub.setLastUpdated(LocalDateTime.now().minusDays(CACHE_TTL_DAYS + 1));
        return albumRepository.save(stub);
    }

    @Transactional
    public ArtistEntity cacheArtist(ArtistResponse response) {
        ArtistEntity entity = artistRepository.findById(response.id())
                .orElseGet(() -> metadataMapper.toEntity(response));

        metadataMapper.updateArtistFromResponse(response, entity);
        entity.setLastUpdated(LocalDateTime.now());

        return artistRepository.save(entity);
    }
}
