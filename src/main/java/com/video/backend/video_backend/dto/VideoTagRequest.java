package com.video.backend.video_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class VideoTagRequest {
    private Integer videoId;
    private List<Integer> tagIds;
}
