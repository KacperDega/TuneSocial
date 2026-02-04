package com.tunesocial.backend.music.dto;

import jakarta.persistence.Embeddable;

@Embeddable
public record AlbumRefDto (
        String id,
        String name
){
}
