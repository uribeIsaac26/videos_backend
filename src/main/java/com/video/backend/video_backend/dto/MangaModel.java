package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.TipoManga;

import java.time.LocalDateTime;
import java.util.List;

public record MangaModel(
        Integer id,
        String title,
        String japaneseTitle,
        String language,
        TipoManga tipo,
        LocalDateTime fechaPublicacion,
        String portadaUrl,
        List<String> artists,
        List<String> groups,
        List<String> parodys,
        List<MangaTagResponse> tags,
        List<MangaPageResponse> paginas
) {
}
