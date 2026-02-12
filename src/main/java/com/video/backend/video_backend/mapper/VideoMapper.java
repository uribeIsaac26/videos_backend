package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.dto.VideoRequest;
import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.dto.VideoModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoMapper {

    @Mapping(target = "thumbnailPath", expression = "java(\"thumbnails/\" + videoRequest.getThumbnailFileName())")
    @Mapping(target = "videoPath", expression = "java(\"video/\" + videoRequest.getVideoFileName())")
    Video toEntity(VideoRequest videoRequest);
    @Mapping(target = "thumbnailUrl", expression = "java(\"/api/videos/\" + video.getId() + \"/thumbnail\")")
    @Mapping(target = "videoUrl", expression = "java(\"/api/videos/\" + video.getId() + \"/video\")")
    VideoModel toModel(Video video);
}
