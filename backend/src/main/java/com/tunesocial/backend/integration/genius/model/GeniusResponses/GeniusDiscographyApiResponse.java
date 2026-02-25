package com.tunesocial.backend.integration.genius.model.GeniusResponses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tunesocial.backend.integration.genius.model.GeniusAlbum;

import java.util.List;

public record GeniusDiscographyApiResponse(
        DiscographyGeniusResponse response
) {
    public record DiscographyGeniusResponse(
            List<GeniusAlbum> albums,
            @JsonProperty("next_page") Integer nextPage
    ) {}
}
