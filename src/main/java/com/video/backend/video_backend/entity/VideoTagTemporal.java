package com.video.backend.video_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_tag_temporal")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoTagTemporal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "video_id", referencedColumnName = "id")
    private Video video;

    @Column(name = "tags_suggest", columnDefinition = "TEXT")
    private String tagsSuggest;

    @Column(name = "confirm")
    private Boolean confirm = false;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
