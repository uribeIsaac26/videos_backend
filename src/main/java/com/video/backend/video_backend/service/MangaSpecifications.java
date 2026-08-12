package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaSearchCriteria;
import com.video.backend.video_backend.dto.MangaTagCriterio;
import com.video.backend.video_backend.entity.Manga;
import com.video.backend.video_backend.entity.MangaTag;
import com.video.backend.video_backend.util.GeneroTag;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import jakarta.persistence.criteria.Join;
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
        List<Specification<Manga>> specs = new ArrayList<>();

        if (criteria.language() != null) {
            String language = criteria.language();
            specs.add((root, query, cb) -> cb.equal(cb.lower(root.get("language")), language.toLowerCase()));
        }

        for (MangaTagCriterio criterio : criteria.tagsIncluir()) {
            specs.add(tieneTag(criterio));
        }

        for (MangaTagCriterio criterio : criteria.tagsExcluir()) {
            specs.add(Specification.not(tieneTag(criterio)));
        }

        for (String grupo : criteria.gruposIncluir()) {
            specs.add(tieneMetadata(TipoMetadatoManga.GROUP, grupo));
        }

        for (String grupo : criteria.gruposExcluir()) {
            specs.add(Specification.not(tieneMetadata(TipoMetadatoManga.GROUP, grupo)));
        }

        for (String artista : criteria.artistasIncluir()) {
            specs.add(tieneMetadata(TipoMetadatoManga.ARTIST, artista));
        }

        for (String artista : criteria.artistasExcluir()) {
            specs.add(Specification.not(tieneMetadata(TipoMetadatoManga.ARTIST, artista)));
        }

        for (String parodia : criteria.parodiasIncluir()) {
            specs.add(tieneMetadata(TipoMetadatoManga.PARODY, parodia));
        }

        for (String parodia : criteria.parodiasExcluir()) {
            specs.add(Specification.not(tieneMetadata(TipoMetadatoManga.PARODY, parodia)));
        }

        return Specification.allOf(specs);
    }

    private static Specification<Manga> tieneMetadata(TipoMetadatoManga tipo, String valor) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Manga> mangaSub = subquery.from(Manga.class);
            Join<Manga, ?> metadataJoin = mangaSub.join("metadataValues");
            subquery.select(cb.literal(1L));

            subquery.where(
                    cb.equal(mangaSub, root),
                    cb.equal(metadataJoin.get("tipo"), tipo),
                    cb.equal(cb.lower(metadataJoin.get("valor")), valor.toLowerCase())
            );

            return cb.exists(subquery);
        };
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
