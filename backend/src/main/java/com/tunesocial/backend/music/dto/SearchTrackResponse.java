package com.tunesocial.backend.music.dto;

public record SearchTrackResponse(
        String id,
        String title,
        String artistNames,
        String imageUrl
) {}
