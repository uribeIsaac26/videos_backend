package com.video.backend.video_backend.dto;

public record VideoTagTemporalResponse(
        Integer id,
        Integer videoId,
        String tagsSuggest,
        Boolean confirm
) {
}
