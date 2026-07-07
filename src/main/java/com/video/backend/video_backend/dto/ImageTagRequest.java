package com.video.backend.video_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageTagRequest {
    private Integer imageId;
    private List<Integer> tagIds;
}
