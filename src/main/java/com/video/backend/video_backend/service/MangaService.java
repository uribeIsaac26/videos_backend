package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaLeidoRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class MangaService {

    private static final int LIMITE_SUGERENCIAS_POR_CATEGORIA = 5;
    private static final Sort ORDEN_MAS_RECIENTES_PRIMERO = Sort.by(Sort.Direction.DESC, "fechaPublicacion");

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
        return mangaRepository.findAll(ordenadoPorFecha(pageable)).map(mangaMapper::toSummary);
    }

    @Transactional
    public MangaModel findById(Integer id) {
        Manga manga = mangaRepository.findById(id).orElseThrow(MangaNotFoundException::new);
        return mangaMapper.toModel(manga);
    }

    @Transactional
    public MangaModel marcarLeido(Integer id, MangaLeidoRequest request) {
        if (request == null || request.leido() == null) {
            throw new IllegalArgumentException("El campo 'leido' es obligatorio");
        }

        Manga manga = mangaRepository.findById(id).orElseThrow(MangaNotFoundException::new);
        manga.setLeido(request.leido());

        return mangaMapper.toModel(mangaRepository.save(manga));
    }

    @Transactional
    public void deleteManga(Integer id) throws IOException {
        Manga manga = mangaRepository.findById(id).orElseThrow(MangaNotFoundException::new);

        Path carpetaPath = Paths.get(mediaBasePath, manga.getCarpeta());
        FileSystemUtils.deleteRecursively(carpetaPath);

        log.info("Manga eliminado");

        mangaRepository.deleteById(id);
    }

    @Transactional
    public Page<MangaSummaryModel> findByAllTags(List<Integer> tagIds, Pageable pageable) {
        Long tagCount = (long) tagIds.size();
        return mangaRepository.findByAllTags(tagIds, tagCount, ordenadoPorFecha(pageable)).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> findByArtist(String nombre, Pageable pageable) {
        return mangaRepository.findByMetadataValue(TipoMetadatoManga.ARTIST, nombre, ordenadoPorFecha(pageable)).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> findByGroup(String nombre, Pageable pageable) {
        return mangaRepository.findByMetadataValue(TipoMetadatoManga.GROUP, nombre, ordenadoPorFecha(pageable)).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> findByParody(String nombre, Pageable pageable) {
        return mangaRepository.findByMetadataValue(TipoMetadatoManga.PARODY, nombre, ordenadoPorFecha(pageable)).map(mangaMapper::toSummary);
    }

    @Transactional
    public Page<MangaSummaryModel> search(String q, Pageable pageable) {
        MangaSearchCriteria criteria = mangaSearchQueryParser.parse(q);
        Specification<Manga> spec = MangaSpecifications.fromCriteria(criteria);
        return mangaRepository.findAll(spec, ordenadoPorFecha(pageable)).map(mangaMapper::toSummary);
    }

    private Pageable ordenadoPorFecha(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ORDEN_MAS_RECIENTES_PRIMERO);
    }

    public List<String> sugerencias(String q) {
        if (q == null || q.isBlank()) return List.of();

        Pageable limite = PageRequest.of(0, LIMITE_SUGERENCIAS_POR_CATEGORIA);
        List<String> sugerencias = new ArrayList<>();

        mangaRepository.findLanguagesStartingWith(q, limite)
                .forEach(valor -> sugerencias.add("language:" + valor.replace(' ', '_')));

        List<String> nombresTag = tagRepository.findNamesStartingWith(q, TagType.MANGA, limite);
        if (!nombresTag.isEmpty()) {
            Set<String> conMale = new HashSet<>(tagRepository.findNombresConMale(nombresTag, TagType.MANGA));
            Set<String> conFemale = new HashSet<>(tagRepository.findNombresConFemale(nombresTag, TagType.MANGA));

            for (String nombre : nombresTag) {
                String valor = nombre.replace(' ', '_');
                boolean tieneGenero = false;

                if (conMale.contains(nombre)) {
                    sugerencias.add("male:" + valor);
                    tieneGenero = true;
                }
                if (conFemale.contains(nombre)) {
                    sugerencias.add("female:" + valor);
                    tieneGenero = true;
                }
                if (!tieneGenero) {
                    sugerencias.add("tag:" + valor);
                }
            }
        }

        mangaMetadataValueRepository.findValoresStartingWith(TipoMetadatoManga.GROUP, q, limite)
                .forEach(valor -> sugerencias.add("group:" + valor.replace(' ', '_')));
        mangaMetadataValueRepository.findValoresStartingWith(TipoMetadatoManga.ARTIST, q, limite)
                .forEach(valor -> sugerencias.add("artist:" + valor.replace(' ', '_')));
        mangaMetadataValueRepository.findValoresStartingWith(TipoMetadatoManga.PARODY, q, limite)
                .forEach(valor -> sugerencias.add("parody:" + valor.replace(' ', '_')));

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
