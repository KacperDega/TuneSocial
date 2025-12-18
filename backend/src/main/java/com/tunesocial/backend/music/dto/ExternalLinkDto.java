package com.tunesocial.backend.music.dto;

public record ExternalLinkDto(
        ExternalLinkType type, // 1.GENIUS, 2.YOUTUBE, 3.SPOTIFY
        String url
) {
}
