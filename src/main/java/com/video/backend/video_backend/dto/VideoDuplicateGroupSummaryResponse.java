package com.video.backend.video_backend.dto;

import java.time.LocalDateTime;

public record VideoDuplicateGroupSummaryResponse(
        Integer id,
        String tagOrigen,
        LocalDateTime dateCreation,
        Integer totalMembers,
        VideoDuplicateMemberResponse preview
) {
}
