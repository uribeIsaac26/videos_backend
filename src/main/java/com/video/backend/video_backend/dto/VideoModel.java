package com.video.backend.video_backend.dto;

import lombok.*;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.MediaType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoModel {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String videoUrl;
}
