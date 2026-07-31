package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.TipoManga;

import java.time.LocalDateTime;

public record MangaSummaryModel(
        Integer id,
        String title,
        TipoManga tipo,
        String language,
        LocalDateTime fechaPublicacion,
        String portadaUrl
) {
}
