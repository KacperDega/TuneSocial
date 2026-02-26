package com.tunesocial.backend.music.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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

    private Instant lastUpdated;
    private Instant discographyLastUpdated;

    public boolean isDiscographyFresh(int days) {
        if (discographyLastUpdated == null) return false;
        return discographyLastUpdated.isAfter(Instant.now().minus(days, ChronoUnit.DAYS));
    }
}
