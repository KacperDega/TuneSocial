package com.tunesocial.backend.music.dto;

public record ArtistResponse(
        String id,
        String name,
        String imageUrl,
        String description
) {
}
