package com.tunesocial.backend.integration.genius.model.GeniusResponses;

import com.tunesocial.backend.integration.genius.model.GeniusAlbum;

public record GeniusAlbumApiResponse(
        AlbumGeniusResponse response
) {
    public record AlbumGeniusResponse(GeniusAlbum album) {
    }
}
