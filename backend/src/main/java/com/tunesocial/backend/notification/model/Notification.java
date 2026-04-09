package com.tunesocial.backend.notification.model;

import com.tunesocial.backend.notification.model.enums.NotificationTargetType;
import com.tunesocial.backend.notification.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter @Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType targetType;

    @Column(nullable = false)
    private String targetId;

    @OneToOne(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private NotificationContextData context;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
