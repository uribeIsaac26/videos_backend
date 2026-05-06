package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.dto.VideoTagTemporalResponse;
import com.video.backend.video_backend.entity.VideoTagTemporal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoTagTemporalMapper {
    @Mapping(target = "videoId", source = "video.id")
    VideoTagTemporalResponse toDto(VideoTagTemporal entity);
}
