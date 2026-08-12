package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaSearchCriteria;
import com.video.backend.video_backend.dto.MangaTagCriterio;
import com.video.backend.video_backend.util.GeneroTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsea consultas de busqueda tipo "spanish male:bajo -female:alto group:circulo":
 * - una palabra suelta filtra por idioma
 * - "language:idioma" filtra por idioma explicitamente
 * - "male:tag" / "female:tag" exigen un tag con ese genero
 * - "tag:tag" exige el tag sin importar el genero
 * - "group:nombre" exige un grupo con ese nombre
 * - "artist:nombre" exige un artista con ese nombre
 * - "parody:nombre" exige una parodia con ese nombre
 * - el prefijo "-" niega cualquiera de los anteriores (excepto el idioma en palabra suelta)
 * - valores con espacios se escriben entre comillas, ej. tag:"full color"
 */
@Component
@Slf4j
public class MangaSearchQueryParser {

    public MangaSearchCriteria parse(String query) {
        List<MangaTagCriterio> incluir = new ArrayList<>();
        List<MangaTagCriterio> excluir = new ArrayList<>();
        List<String> gruposIncluir = new ArrayList<>();
        List<String> gruposExcluir = new ArrayList<>();
        List<String> artistasIncluir = new ArrayList<>();
        List<String> artistasExcluir = new ArrayList<>();
        List<String> parodiasIncluir = new ArrayList<>();
        List<String> parodiasExcluir = new ArrayList<>();
        String language = null;

        if (query == null || query.isBlank()) {
            return new MangaSearchCriteria(null, incluir, excluir, gruposIncluir, gruposExcluir, artistasIncluir, artistasExcluir, parodiasIncluir, parodiasExcluir);
        }

        for (String token : tokenizar(query)) {
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

            if (prefijo.equals("language")) {
                if (negado) {
                    log.warn("Se ignora el termino de idioma negado '{}': no soportado", token);
                } else {
                    language = valor;
                }
                continue;
            }

            if (prefijo.equals("group")) {
                (negado ? gruposExcluir : gruposIncluir).add(valor);
                continue;
            }

            if (prefijo.equals("artist")) {
                (negado ? artistasExcluir : artistasIncluir).add(valor);
                continue;
            }

            if (prefijo.equals("parody")) {
                (negado ? parodiasExcluir : parodiasIncluir).add(valor);
                continue;
            }

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

        return new MangaSearchCriteria(language, incluir, excluir, gruposIncluir, gruposExcluir, artistasIncluir, artistasExcluir, parodiasIncluir, parodiasExcluir);
    }

    private List<String> tokenizar(String query) {
        List<String> tokens = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroDeComillas = false;

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);

            if (c == '"') {
                dentroDeComillas = !dentroDeComillas;
                continue;
            }

            if (Character.isWhitespace(c) && !dentroDeComillas) {
                if (!actual.isEmpty()) {
                    tokens.add(actual.toString());
                    actual.setLength(0);
                }
                continue;
            }

            actual.append(c);
        }

        if (!actual.isEmpty()) {
            tokens.add(actual.toString());
        }

        return tokens;
    }
}
