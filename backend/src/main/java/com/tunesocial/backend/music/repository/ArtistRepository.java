package com.tunesocial.backend.music.repository;

import com.tunesocial.backend.music.model.ArtistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<ArtistEntity, String> {
}
