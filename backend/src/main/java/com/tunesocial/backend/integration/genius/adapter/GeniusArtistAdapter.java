package com.tunesocial.backend.integration.genius.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.tunesocial.backend.integration.genius.model.GeniusArtist;
import com.tunesocial.backend.integration.genius.model.GeniusArtistApiResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import org.springframework.stereotype.Component;

@Component
public class GeniusArtistAdapter {

    public ArtistResponse adapt(GeniusArtistApiResponse res) {
        GeniusArtist artist = res.response().artist();

        String description = unwrapDescription(artist.description());

        return new ArtistResponse(
                artist.id(),
                artist.name(),
                artist.headerImageUrl(),
                description
        );
    }

    private String unwrapDescription(JsonNode description) {
        if (description == null || description.isNull()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        JsonNode dom = description.path("dom");

        extractText(dom, sb);

        return sb.toString().trim();
    }

    private void extractText(JsonNode node, StringBuilder sb) {
        if (node.isTextual()) {
            sb.append(node.asText());
        }

        if (node.has("children")) {
            for (JsonNode child : node.get("children")) {
                extractText(child, sb);
            }

        }
    }
}
