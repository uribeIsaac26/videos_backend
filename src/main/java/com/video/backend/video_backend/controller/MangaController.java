package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.MangaModel;
import com.video.backend.video_backend.dto.MangaPageResponse;
import com.video.backend.video_backend.dto.MangaSummaryModel;
import com.video.backend.video_backend.service.MangaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("api/mangas")
@RequiredArgsConstructor
public class MangaController {
    private final MangaService mangaService;

    @GetMapping
    public ResponseEntity<Page<MangaSummaryModel>> findAll(Pageable pageable) {
        return ResponseEntity.ok(mangaService.findAll(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<MangaModel> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(mangaService.findById(id));
    }

    @GetMapping("{mangaId}/paginas")
    public ResponseEntity<Page<MangaPageResponse>> findPaginas(
            @PathVariable Integer mangaId,
            Pageable pageable) {
        return ResponseEntity.ok(mangaService.findPaginas(mangaId, pageable));
    }

    @GetMapping("{mangaId}/paginas/{orden}/imagen")
    public ResponseEntity<Resource> findPaginaImagen(
            @PathVariable Integer mangaId,
            @PathVariable Integer orden
    ) throws IOException {
        Resource resource = mangaService.findPaginaImagen(mangaId, orden);

        Path path = resource.getFile().toPath();
        String contentType = Files.probeContentType(path);

        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/tag")
    public ResponseEntity<Page<MangaSummaryModel>> getMangasByTag(
            @RequestParam List<Integer> tagIds,
            Pageable pageable) {
        return ResponseEntity.ok(mangaService.findByAllTags(tagIds, pageable));
    }

    @GetMapping("/artist")
    public ResponseEntity<Page<MangaSummaryModel>> getMangasByArtist(
            @RequestParam String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(mangaService.findByArtist(nombre, pageable));
    }

    @GetMapping("/group")
    public ResponseEntity<Page<MangaSummaryModel>> getMangasByGroup(
            @RequestParam String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(mangaService.findByGroup(nombre, pageable));
    }

    @GetMapping("/parody")
    public ResponseEntity<Page<MangaSummaryModel>> getMangasByParody(
            @RequestParam String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(mangaService.findByParody(nombre, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MangaSummaryModel>> search(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(mangaService.search(q, pageable));
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<List<String>> sugerencias(@RequestParam String q) {
        return ResponseEntity.ok(mangaService.sugerencias(q));
    }
}
