package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaSearchCriteria;
import com.video.backend.video_backend.dto.MangaTagCriterio;
import com.video.backend.video_backend.util.GeneroTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsea consultas de busqueda tipo "spanish male:bajo -female:alto":
 * - una palabra suelta filtra por idioma
 * - "male:tag" / "female:tag" exigen un tag con ese genero
 * - "tag:tag" exige el tag sin importar el genero
 * - el prefijo "-" niega cualquiera de los anteriores
 */
@Component
@Slf4j
public class MangaSearchQueryParser {

    public MangaSearchCriteria parse(String query) {
        List<MangaTagCriterio> incluir = new ArrayList<>();
        List<MangaTagCriterio> excluir = new ArrayList<>();
        String language = null;

        if (query == null || query.isBlank()) {
            return new MangaSearchCriteria(null, incluir, excluir);
        }

        for (String token : query.trim().split("\\s+")) {
            boolean negado = token.startsWith("-");
            String cuerpo = negado ? token.substring(1) : token;

            if (cuerpo.isBlank()) continue;

            int separador = cuerpo.indexOf(':');
            if (separador < 0) {
                if (negado) {
                    log.warn("Se ignora el termino de idioma negado '{}': no soportado", token);
                } else {
                    language = cuerpo;
                }
                continue;
            }

            String prefijo = cuerpo.substring(0, separador).toLowerCase();
            String valor = cuerpo.substring(separador + 1);

            if (valor.isBlank()) continue;

            GeneroTag genero = switch (prefijo) {
                case "male" -> GeneroTag.MALE;
                case "female" -> GeneroTag.FEMALE;
                case "tag" -> GeneroTag.CUALQUIERA;
                default -> null;
            };

            if (genero == null) {
                log.warn("Se ignora el termino '{}': prefijo desconocido", token);
                continue;
            }

            MangaTagCriterio criterio = new MangaTagCriterio(valor, genero);
            (negado ? excluir : incluir).add(criterio);
        }

        return new MangaSearchCriteria(language, incluir, excluir);
    }
}
