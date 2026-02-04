package com.tunesocial.backend.music.dto;

import jakarta.persistence.Embeddable;

@Embeddable
public record ExternalLinkDto(
        ExternalLinkType type, // 1.GENIUS, 2.YOUTUBE, 3.SPOTIFY
        String url
) {
}
