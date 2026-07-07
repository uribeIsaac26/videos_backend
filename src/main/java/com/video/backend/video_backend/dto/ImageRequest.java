package com.video.backend.video_backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ImageRequest {
    private String title;
    private String imageFileName;
    private Long size;
    private String contentType;
}
