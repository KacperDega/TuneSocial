package com.tunesocial.backend.post.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedItemContext {

    @Column(name = "target_title", length = 255)
    private String title;

    @Column(name = "target_subtitle", length = 255)
    private String subtitle;
    @Column(name = "target_image_url", length = 1024)
    private String imageUrl;
}
