package com.video.backend.video_backend.service;

import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.repository.TagRepository;
import com.video.backend.video_backend.util.TagType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Antes de separar tags por tipo de contenido, video/image/manga compartian el mismo pool
 * de tags. Al arrancar, esta clase:
 * 1) separa cualquier tag usado por mas de un tipo de contenido en filas independientes
 *    (reasignando las filas de la tabla de union correspondiente al nuevo tag), y
 * 2) rellena el type de los tags legacy (creados con el default 'VIDEO' de la columna)
 *    segun en que tabla de union aparecen.
 * Es idempotente: en cada tag ya separado y con type correcto, ambos pasos no hacen nada.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TagTypeMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        splitTagsUsadosPorVariosTipos();
        backfillTypeDeTagsLegacy();
    }

    private void splitTagsUsadosPorVariosTipos() {
        List<Map<String, Object>> conflictivos = jdbcTemplate.queryForList(
                "SELECT DISTINCT t.id AS id, t.name AS name FROM tag t " +
                        "WHERE (EXISTS (SELECT 1 FROM video_tag vt WHERE vt.tag_id = t.id) AND EXISTS (SELECT 1 FROM image_tag it WHERE it.tag_id = t.id)) " +
                        "   OR (EXISTS (SELECT 1 FROM video_tag vt WHERE vt.tag_id = t.id) AND EXISTS (SELECT 1 FROM manga_tag mt WHERE mt.tag_id = t.id)) " +
                        "   OR (EXISTS (SELECT 1 FROM image_tag it WHERE it.tag_id = t.id) AND EXISTS (SELECT 1 FROM manga_tag mt WHERE mt.tag_id = t.id))"
        );

        if (conflictivos.isEmpty()) return;

        log.info("Separando {} tags usados por mas de un tipo de contenido", conflictivos.size());

        for (Map<String, Object> fila : conflictivos) {
            Integer tagId = (Integer) fila.get("id");
            String name = (String) fila.get("name");
            separar(tagId, name);
        }
    }

    private void separar(Integer tagId, String name) {
        boolean enVideo = existeEn("video_tag", tagId);
        boolean enImage = existeEn("image_tag", tagId);
        boolean enManga = existeEn("manga_tag", tagId);

        TagType principal = enVideo ? TagType.VIDEO : enImage ? TagType.IMAGE : TagType.MANGA;
        jdbcTemplate.update("UPDATE tag SET type = ? WHERE id = ?", principal.name(), tagId);

        if (enImage && principal != TagType.IMAGE) {
            reasignar(name, TagType.IMAGE, "image_tag", tagId);
        }
        if (enManga && principal != TagType.MANGA) {
            reasignar(name, TagType.MANGA, "manga_tag", tagId);
        }
        if (enVideo && principal != TagType.VIDEO) {
            reasignar(name, TagType.VIDEO, "video_tag", tagId);
        }
    }

    private boolean existeEn(String tablaJoin, Integer tagId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tablaJoin + " WHERE tag_id = ?", Integer.class, tagId);
        return count != null && count > 0;
    }

    private void reasignar(String name, TagType tipo, String tablaJoin, Integer tagIdOriginal) {
        Tag nuevo = new Tag();
        nuevo.setName(name);
        nuevo.setType(tipo);
        Tag guardado = tagRepository.save(nuevo);

        jdbcTemplate.update(
                "UPDATE " + tablaJoin + " SET tag_id = ? WHERE tag_id = ?",
                guardado.getId(), tagIdOriginal
        );
    }

    private void backfillTypeDeTagsLegacy() {
        jdbcTemplate.update(
                "UPDATE tag t SET t.type = 'VIDEO' " +
                        "WHERE t.type <> 'VIDEO' AND EXISTS (SELECT 1 FROM video_tag vt WHERE vt.tag_id = t.id)"
        );
        jdbcTemplate.update(
                "UPDATE tag t SET t.type = 'IMAGE' " +
                        "WHERE t.type <> 'IMAGE' AND EXISTS (SELECT 1 FROM image_tag it WHERE it.tag_id = t.id)"
        );
        jdbcTemplate.update(
                "UPDATE tag t SET t.type = 'MANGA' " +
                        "WHERE t.type <> 'MANGA' AND EXISTS (SELECT 1 FROM manga_tag mt WHERE mt.tag_id = t.id)"
        );
    }
}
