package com.tunesocial.backend.integration.genius.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.tunesocial.backend.integration.genius.mapper.GeniusAlbumMapper;
import com.tunesocial.backend.integration.genius.model.*;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class GeniusArtistAdapter {

    // TODO: MAPPER
    private final GeniusAlbumMapper geniusAlbumMapper;

    public ArtistResponse adaptArtist(GeniusArtistApiResponse res) {
        GeniusArtist artist = res.response().artist();

        String description = unwrapDescription(artist.description());

        return new ArtistResponse(
                artist.id(),
                artist.name(),
                artist.headerImageUrl(),
                description
        );
    }

    public List<AlbumSummaryResponse> adaptDiscography(GeniusDiscographyApiResponse res) {
        List<GeniusAlbum> albums = res.response().albums();

        return albums.stream()
                .map(geniusAlbumMapper::toAlbumSummary)
                .collect(Collectors.toList());
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
