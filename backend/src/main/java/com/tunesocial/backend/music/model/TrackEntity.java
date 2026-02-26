package com.tunesocial.backend.music.model;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.dto.ExternalLinkDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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

    private Instant lastUpdated;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackEntity that = (TrackEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
