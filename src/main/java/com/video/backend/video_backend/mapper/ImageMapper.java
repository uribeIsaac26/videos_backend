package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.dto.ImageModel;
import com.video.backend.video_backend.dto.ImageRequest;
import com.video.backend.video_backend.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImageMapper {

    @Mapping(target = "imagePath", expression = "java(\"images/\" + imageRequest.getImageFileName())")
    Image toEntity(ImageRequest imageRequest);

    @Mapping(target = "imageUrl", expression = "java(\"/api/images/\" + image.getId() + \"/image\")")
    ImageModel toModel(Image image);
}
