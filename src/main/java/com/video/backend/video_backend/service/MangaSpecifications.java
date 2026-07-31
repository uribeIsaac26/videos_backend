package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaSearchCriteria;
import com.video.backend.video_backend.dto.MangaTagCriterio;
import com.video.backend.video_backend.entity.Manga;
import com.video.backend.video_backend.entity.MangaTag;
import com.video.backend.video_backend.util.GeneroTag;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MangaSpecifications {

    private MangaSpecifications() {
    }

    public static Specification<Manga> fromCriteria(MangaSearchCriteria criteria) {
        Specification<Manga> spec = Specification.where((Specification<Manga>) null);

        if (criteria.language() != null) {
            String language = criteria.language();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.toLowerCase()));
        }

        for (MangaTagCriterio criterio : criteria.tagsIncluir()) {
            spec = spec.and(tieneTag(criterio));
        }

        for (MangaTagCriterio criterio : criteria.tagsExcluir()) {
            spec = spec.and(Specification.not(tieneTag(criterio)));
        }

        return spec;
    }

    private static Specification<Manga> tieneTag(MangaTagCriterio criterio) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<MangaTag> mangaTag = subquery.from(MangaTag.class);
            subquery.select(cb.literal(1L));

            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.equal(mangaTag.get("manga"), root));
            condiciones.add(cb.equal(cb.lower(mangaTag.get("tag").get("name")), criterio.nombre().toLowerCase()));

            if (criterio.genero() == GeneroTag.MALE) {
                condiciones.add(cb.isTrue(mangaTag.get("esMale")));
            } else if (criterio.genero() == GeneroTag.FEMALE) {
                condiciones.add(cb.isTrue(mangaTag.get("esFemale")));
            }

            subquery.where(condiciones.toArray(new Predicate[0]));
            return cb.exists(subquery);
        };
    }
}
