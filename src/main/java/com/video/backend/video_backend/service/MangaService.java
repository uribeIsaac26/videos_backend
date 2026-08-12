package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaModel;
import com.video.backend.video_backend.dto.MangaPageResponse;
import com.video.backend.video_backend.dto.MangaSearchCriteria;
import com.video.backend.video_backend.dto.MangaSummaryModel;
import com.video.backend.video_backend.entity.Manga;
import com.video.backend.video_backend.entity.MangaPage;
import com.video.backend.video_backend.excepcion.MangaNotFoundException;
import com.video.backend.video_backend.excepcion.MangaPageNotFoundException;
import com.video.backend.video_backend.mapper.MangaMapper;
import com.video.backend.video_backend.repository.MangaMetadataValueRepository;
import com.video.backend.video_backend.repository.MangaPageRepository;
import com.video.backend.video_backend.repository.MangaRepository;
import com.video.backend.video_backend.repository.TagRepository;
import com.video.backend.video_backend.util.TagType;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MangaService {

    private static final int LIMITE_SUGERENCIAS_POR_CATEGORIA = 5;

    private final MangaRepository mangaRepository;
    private final MangaPageRepository mangaPageRepository;
    private final TagRepository tagRepository;
    private final MangaMetadataValueRepository mangaMetadataValueRepository;
    private final MangaMapper mangaMapper;
    private final MangaSearchQueryParser mangaSearchQueryParser;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    @Transactional
    public Page<MangaSummaryModel> findAll(Pageable pageable) {
        return mangaRepository.findAll(pageable).map(mangaMapper::toSummary);
    }

    @Transactional
    public MangaModel findById(Integer id) {
        Manga manga = mangaRepository.findById(id).orElseThrow(MangaNotFoundException::new);
        return mangaMapper.toModel(manga);
    }

    @Transactional
    public Page<MangaSummaryModel> findByAllTags(List<Integer> tagIds, Pageable pageable) {
        Long tagCount = (long) tagIds.size();
        return mangaRepository.findByAllTags(tagIds, tagCount, pageable).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> findByArtist(String nombre, Pageable pageable) {
        return mangaRepository.findByMetadataValue(TipoMetadatoManga.ARTIST, nombre, pageable).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> findByGroup(String nombre, Pageable pageable) {
        return mangaRepository.findByMetadataValue(TipoMetadatoManga.GROUP, nombre, pageable).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> findByParody(String nombre, Pageable pageable) {
        return mangaRepository.findByMetadataValue(TipoMetadatoManga.PARODY, nombre, pageable).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> search(String q, Pageable pageable) {
        MangaSearchCriteria criteria = mangaSearchQueryParser.parse(q);
        Specification<Manga> spec = MangaSpecifications.fromCriteria(criteria);
        return mangaRepository.findAll(spec, pageable).map(mangaMapper::toSummary);
    }

    public List<String> sugerencias(String q) {
        if (q == null || q.isBlank()) return List.of();

        Pageable limite = PageRequest.of(0, LIMITE_SUGERENCIAS_POR_CATEGORIA);
        List<String> sugerencias = new ArrayList<>();

        mangaRepository.findLanguagesStartingWith(q, limite)
                .forEach(valor -> sugerencias.add("language:" + valor));
        tagRepository.findNamesStartingWith(q, TagType.MANGA, limite)
                .forEach(valor -> sugerencias.add("tag:" + valor));
        mangaMetadataValueRepository.findValoresStartingWith(TipoMetadatoManga.GROUP, q, limite)
                .forEach(valor -> sugerencias.add("group:" + valor));
        mangaMetadataValueRepository.findValoresStartingWith(TipoMetadatoManga.ARTIST, q, limite)
                .forEach(valor -> sugerencias.add("artist:" + valor));
        mangaMetadataValueRepository.findValoresStartingWith(TipoMetadatoManga.PARODY, q, limite)
                .forEach(valor -> sugerencias.add("parody:" + valor));

        return sugerencias;
    }

    public Page<MangaPageResponse> findPaginas(Integer mangaId, Pageable pageable) {
        if (!mangaRepository.existsById(mangaId)) {
            throw new MangaNotFoundException();
        }

        return mangaPageRepository.findByMangaIdOrderByOrden(mangaId, pageable)
                .map(pagina -> mangaMapper.toPaginaResponse(mangaId, pagina));
    }

    public Resource findPaginaImagen(Integer mangaId, Integer orden) {
        MangaPage pagina = mangaPageRepository.findByMangaIdAndOrden(mangaId, orden)
                .orElseThrow(MangaPageNotFoundException::new);

        Path path = Paths.get(mediaBasePath)
                .resolve(pagina.getManga().getCarpeta())
                .resolve(pagina.getNombreArchivo())
                .normalize();

        try {
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new MangaPageNotFoundException();
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
