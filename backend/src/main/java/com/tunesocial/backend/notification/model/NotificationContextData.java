package com.tunesocial.backend.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_contexts")
@Getter @Setter
public class NotificationContextData {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "notification_id")
    private Notification notification;

    private String imageUrl;

    @Column(length = 255)
    private String textSnippet;

    private String actionUrl;
}
