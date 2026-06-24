package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.VideoStatus;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoModel {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private String videoUrl;
    private Long size;
    private VideoStatus status;
    private String errorMessage;
    private Set<TagResponse> tags;
}
