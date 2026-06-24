package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoStatusResponse {
    private Integer id;
    private VideoStatus status;
    private String errorMessage;
}
