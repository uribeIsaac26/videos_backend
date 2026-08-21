package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.TipoManga;

import java.time.LocalDateTime;
import java.util.List;

public record MangaSummaryModel(
        Integer id,
        String title,
        TipoManga tipo,
        String language,
        LocalDateTime fechaPublicacion,
        String portadaUrl,
        List<String> artists,
        List<String> parodys,
        List<MangaTagResponse> tags,
        Boolean leido
) {
}
