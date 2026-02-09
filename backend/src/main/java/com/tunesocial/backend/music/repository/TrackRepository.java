package com.tunesocial.backend.music.repository;

import com.tunesocial.backend.music.model.TrackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<TrackEntity, String> {
}
