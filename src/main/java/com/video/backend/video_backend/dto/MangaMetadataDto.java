package com.video.backend.video_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MangaMetadataDto(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("japaneseTitle") String japaneseTitle,
        @JsonProperty("language") String language,
        @JsonProperty("type") String type,
        @JsonProperty("date") String date,
        @JsonProperty("artists") List<String> artists,
        @JsonProperty("groups") List<String> groups,
        @JsonProperty("parodys") List<String> parodys,
        @JsonProperty("tags") List<MangaMetadataTagDto> tags,
        @JsonProperty("files") List<MangaMetadataFileDto> files,
        @JsonProperty("coverUrl") String coverUrl
) {
}
