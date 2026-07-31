package com.video.backend.video_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MangaMetadataTagDto(
        @JsonProperty("tag") String tag,
        @JsonProperty("female") Integer female,
        @JsonProperty("male") Integer male
) {
}
