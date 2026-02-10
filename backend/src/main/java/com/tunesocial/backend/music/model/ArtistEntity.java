package com.tunesocial.backend.music.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cached_artists")
@Getter @Setter
public class ArtistEntity implements CacheableEntity {
    @Id
    private String id;
    private String name;
    private String imageUrl;

    @Lob
    private String description;

    private LocalDateTime lastUpdated;
}
