package com.video.backend.video_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.MediaType;

@Getter
@AllArgsConstructor
public class VideoStream {
    private final ResourceRegion region;
    private final MediaType mediaType;
}