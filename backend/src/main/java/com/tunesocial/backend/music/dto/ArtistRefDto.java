package com.tunesocial.backend.music.dto;

import jakarta.persistence.Embeddable;

@Embeddable
public record ArtistRefDto(
        String id,
        String name
) {
}
