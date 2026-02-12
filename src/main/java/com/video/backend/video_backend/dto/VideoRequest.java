package com.video.backend.video_backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VideoRequest {
    private String title;
    private String videoFileName;
    private String thumbnailFileName;
}
