package com.video.backend.video_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "video_path")
    private String videoPath;
}
