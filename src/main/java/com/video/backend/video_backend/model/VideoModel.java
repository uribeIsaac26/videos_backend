package com.video.backend.video_backend.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoModel {
    private Integer id;
    private String title;
    private String thumbnailPath;
    private String videoPath;
}
