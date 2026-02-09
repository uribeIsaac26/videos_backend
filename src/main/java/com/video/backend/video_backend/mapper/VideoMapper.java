package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.model.VideoModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoMapper {

    Video toEntity(VideoModel videoModel);
    @Mapping(target = "thumbnailUrl", expression = "java(\"/api/videos/\" + video.getId() + \"/thumbnail\")")
    @Mapping(target = "videoUrl", expression = "java(\"/api/videos/\" + video.getId() + \"/video\")")
    VideoModel toModel(Video video);
}
