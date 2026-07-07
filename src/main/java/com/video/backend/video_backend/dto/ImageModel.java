package com.video.backend.video_backend.dto;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageModel {
    private Integer id;
    private String title;
    private String imageUrl;
    private Long size;
    private String contentType;
    private Set<TagResponse> tags;
}
