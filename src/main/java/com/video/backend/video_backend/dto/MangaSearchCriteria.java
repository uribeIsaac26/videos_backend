package com.video.backend.video_backend.dto;

import java.util.List;

public record MangaSearchCriteria(
        String language,
        List<MangaTagCriterio> tagsIncluir,
        List<MangaTagCriterio> tagsExcluir,
        List<String> gruposIncluir,
        List<String> gruposExcluir,
        List<String> artistasIncluir,
        List<String> artistasExcluir
) {
}
