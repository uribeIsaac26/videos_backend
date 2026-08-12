package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.TagType;

public record TagRequest(
        String name,
        TagType type
) {}
