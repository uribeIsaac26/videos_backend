package com.video.backend.video_backend.dto;

import com.video.backend.video_backend.util.GeneroTag;

public record MangaTagCriterio(
        String nombre,
        GeneroTag genero
) {
}
