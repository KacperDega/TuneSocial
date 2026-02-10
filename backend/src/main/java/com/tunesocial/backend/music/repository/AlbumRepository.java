package com.tunesocial.backend.music.repository;

import com.tunesocial.backend.music.model.AlbumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<AlbumEntity, String> {
    List<AlbumEntity> findAllByArtists_Id(String id);
}
