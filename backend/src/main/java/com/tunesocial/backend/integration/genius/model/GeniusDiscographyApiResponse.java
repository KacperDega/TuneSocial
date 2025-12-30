package com.tunesocial.backend.integration.genius.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeniusDiscographyApiResponse(
        DiscographyGeniusResponse response
) {
    public record DiscographyGeniusResponse(
            List<GeniusAlbum> albums,
            @JsonProperty("next_page") Integer nextPage
    ) {}
}
