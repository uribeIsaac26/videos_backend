package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.model.VideoModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoMapper {

    Video toEntity(VideoModel videoModel);
    VideoModel toModel(Video video);
}
