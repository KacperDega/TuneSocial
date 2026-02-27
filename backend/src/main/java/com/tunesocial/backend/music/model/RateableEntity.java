package com.tunesocial.backend.music.model;

import com.tunesocial.backend.music.dto.ArtistRefDto;

import java.util.List;

public interface RateableEntity {
    String getTitle();
    String getImageUrl();
    List<ArtistRefDto> getArtists();
}
