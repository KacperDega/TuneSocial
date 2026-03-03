package com.tunesocial.backend.social.repository;

import com.tunesocial.backend.social.model.FeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {
}
