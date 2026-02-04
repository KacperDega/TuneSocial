package com.tunesocial.backend.music.model;

import java.time.LocalDateTime;

public interface CacheableEntity {
    String getId();

    LocalDateTime getLastUpdated();

    void setLastUpdated(LocalDateTime lastUpdated);

    default boolean isFresh(int ttlDays) {
        return getLastUpdated() != null &&
                getLastUpdated().isAfter(LocalDateTime.now().minusDays(ttlDays));
    }
}
