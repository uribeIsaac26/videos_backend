package com.video.backend.video_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VideoDuplicateGroupDetailResponse(
        Integer id,
        String tagOrigen,
        LocalDateTime dateCreation,
        Integer totalMembers,
        List<VideoDuplicateMemberResponse> members
) {
}
