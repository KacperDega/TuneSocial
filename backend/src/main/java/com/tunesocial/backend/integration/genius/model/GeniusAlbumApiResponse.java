package com.tunesocial.backend.integration.genius.model;

public record GeniusAlbumApiResponse(
        AlbumGeniusResponse response
) {
    public record AlbumGeniusResponse(GeniusAlbum album) {
    }
}
