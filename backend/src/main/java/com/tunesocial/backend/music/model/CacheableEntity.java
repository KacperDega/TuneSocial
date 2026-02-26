package com.tunesocial.backend.music.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public interface CacheableEntity {
    String getId();

    Instant getLastUpdated();

    void setLastUpdated(Instant lastUpdated);

    default boolean isFresh(int ttlDays) {
        return getLastUpdated() != null &&
                getLastUpdated().isAfter(Instant.now().minus(ttlDays, ChronoUnit.DAYS));
    }
}
