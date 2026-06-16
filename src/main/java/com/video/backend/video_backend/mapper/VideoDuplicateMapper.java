package com.video.backend.video_backend.mapper;

import com.video.backend.video_backend.dto.VideoDuplicateGroupDetailResponse;
import com.video.backend.video_backend.dto.VideoDuplicateGroupSummaryResponse;
import com.video.backend.video_backend.dto.VideoDuplicateMemberResponse;
import com.video.backend.video_backend.dto.VideoPreviewDto;
import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.entity.VideoDuplicateGroup;
import com.video.backend.video_backend.entity.VideoDuplicateMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoDuplicateMapper {

    @Mapping(target = "thumbnailUrl", expression = "java(\"/api/videos/\" + video.getId() + \"/thumbnail\")")
    VideoPreviewDto toVideoPreview(Video video);

    @Mapping(target = "video", source = "video")
    VideoDuplicateMemberResponse toMemberResponse(VideoDuplicateMember member);

    @Mapping(target = "totalMembers", expression = "java(group.getMembers().size())")
    @Mapping(target = "preview", expression = "java(group.getMembers().isEmpty() ? null : toMemberResponse(group.getMembers().get(0)))")
    VideoDuplicateGroupSummaryResponse toSummary(VideoDuplicateGroup group);

    @Mapping(target = "totalMembers", expression = "java(group.getMembers().size())")
    @Mapping(target = "members", source = "members")
    VideoDuplicateGroupDetailResponse toDetail(VideoDuplicateGroup group);
}
