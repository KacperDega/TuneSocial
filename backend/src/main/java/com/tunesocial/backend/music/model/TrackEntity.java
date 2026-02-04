package com.tunesocial.backend.music.model;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.dto.ExternalLinkDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cached_tracks")
@Getter @Setter
public class TrackEntity implements CacheableEntity {
    @Id
    private String id;
    private String title;
    private String imageUrl;
    private String releaseDate;

    @ElementCollection
    private List<ArtistRefDto> artists;

    @ElementCollection
    private List<ExternalLinkDto> links;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private AlbumEntity album;

    private LocalDateTime lastUpdated;
}
