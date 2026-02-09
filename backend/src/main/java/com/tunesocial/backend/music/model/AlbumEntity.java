package com.tunesocial.backend.music.model;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cached_albums")
@Getter @Setter
public class AlbumEntity implements CacheableEntity {
    @Id
    private String id;
    private String title;
    private String imageUrl;
    private String releaseDate;

    @ElementCollection
    private List<ArtistRefDto> artists;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackEntity> tracks = new ArrayList<>();

    private LocalDateTime lastUpdated;

    public void addTrack(TrackEntity track) {
        if (track == null) {
            return;
        }

        if (!this.tracks.contains(track)) {
            this.tracks.add(track);
            track.setAlbum(this);
        }
    }
}
