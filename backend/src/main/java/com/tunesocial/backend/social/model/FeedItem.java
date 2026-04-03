package com.tunesocial.backend.social.model;

import com.tunesocial.backend.social.model.enums.FeedItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "feed_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "type", "referenceId"} )
})
@Getter @Setter
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedItemType type;

    @Column(nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
