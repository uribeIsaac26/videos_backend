package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.TagType;

public record TagResponse(
        Integer id,
        String name,
        TagType type
) {
}
