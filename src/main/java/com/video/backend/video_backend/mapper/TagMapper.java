package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.dto.TagRequest;
import com.video.backend.video_backend.dto.TagResponse;
import com.video.backend.video_backend.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    TagResponse toResponse(Tag entity);

    Tag toEntity(TagRequest request);
}
