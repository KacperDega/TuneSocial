package com.tunesocial.backend.integration.genius.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record GeniusArtist(
        String id,
        String name,
        @JsonProperty("header_image_url") String headerImageUrl,
        @JsonProperty("description") JsonNode description
) {
}
