package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.Accion;

public record VideoDuplicateMemberResponse(
        Integer id,
        Float similitud,
        Boolean revisado,
        Accion accion,
        VideoPreviewDto video
) {
}
