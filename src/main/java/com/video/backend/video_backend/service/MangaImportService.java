package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.MangaMetadataDto;
import com.video.backend.video_backend.dto.MangaMetadataFileDto;
import com.video.backend.video_backend.dto.MangaMetadataTagDto;
import com.video.backend.video_backend.entity.Manga;
import com.video.backend.video_backend.entity.MangaMetadataValue;
import com.video.backend.video_backend.entity.MangaPage;
import com.video.backend.video_backend.entity.MangaTag;
import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.repository.MangaMetadataValueRepository;
import com.video.backend.video_backend.repository.MangaRepository;
import com.video.backend.video_backend.repository.TagRepository;
import com.video.backend.video_backend.util.TipoManga;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
public class MangaImportService {

    private final MangaRepository mangaRepository;
    private final MangaMetadataValueRepository mangaMetadataValueRepository;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    private static final String METADATA_FILE_NAME = "metadata.json";
    private static final DateTimeFormatter FECHA_METADATA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");

    @Scheduled(fixedDelayString = "${app.manga.import-interval-ms:300000}")
    public void importarDesdeLibrary() {
        Path libraryDir = Paths.get(mediaBasePath, "mangaLibrary");

        if (!Files.isDirectory(libraryDir)) {
            return;
        }

        try (Stream<Path> entries = Files.list(libraryDir)) {
            entries.filter(Files::isDirectory).forEach(this::procesarCarpeta);
        } catch (IOException e) {
            log.error("No se pudo listar el contenido de mangaLibrary", e);
        }
    }

    private void procesarCarpeta(Path carpeta) {
        Path metadataPath = carpeta.resolve(METADATA_FILE_NAME);

        if (!Files.exists(metadataPath)) {
            log.warn("Carpeta '{}' en mangaLibrary sin {}, se omite", carpeta.getFileName(), METADATA_FILE_NAME);
            return;
        }

        MangaMetadataDto dto;
        try {
            dto = objectMapper.readValue(metadataPath, MangaMetadataDto.class);
        } catch (JacksonException e) {
            log.error("No se pudo parsear {} en '{}', se omite", METADATA_FILE_NAME, carpeta.getFileName(), e);
            return;
        }

        if (dto.id() == null) {
            log.warn("Carpeta '{}' con metadata sin id, se omite", carpeta.getFileName());
            return;
        }

        if (mangaRepository.findBySourceId(dto.id()).isPresent()) {
            return;
        }

        try {
            importarManga(dto, carpeta);
            log.info("Manga '{}' (id={}) importado a la base de datos", dto.title(), dto.id());
        } catch (Exception e) {
            log.error("No se pudo importar el manga '{}' (id={})", carpeta.getFileName(), dto.id(), e);
        }
    }

    private void importarManga(MangaMetadataDto dto, Path carpeta) {
        Manga manga = new Manga();
        manga.setSourceId(dto.id());
        manga.setTitle(dto.title());
        manga.setJapaneseTitle(dto.japaneseTitle());
        manga.setLanguage(dto.language());
        manga.setTipo(mapTipo(dto.type()));
        manga.setFechaPublicacion(parseFecha(dto.date()));
        manga.setCoverUrl(dto.coverUrl());
        manga.setCarpeta("mangaLibrary/" + carpeta.getFileName());

        agregarPaginas(manga, carpeta, dto.files());
        agregarTags(manga, dto.tags());
        agregarMetadataValues(manga, TipoMetadatoManga.ARTIST, dto.artists());
        agregarMetadataValues(manga, TipoMetadatoManga.GROUP, dto.groups());
        agregarMetadataValues(manga, TipoMetadatoManga.PARODY, dto.parodys());

        mangaRepository.save(manga);
    }

    private void agregarPaginas(Manga manga, Path carpeta, List<MangaMetadataFileDto> files) {
        if (files == null) return;

        List<Path> archivosReales;
        try (Stream<Path> entries = Files.list(carpeta)) {
            archivosReales = entries
                    .filter(Files::isRegularFile)
                    .filter(archivo -> !archivo.getFileName().toString().equals(METADATA_FILE_NAME))
                    .sorted(Comparator.comparing(archivo -> archivo.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo listar los archivos de '" + carpeta.getFileName() + "'", e);
        }

        if (archivosReales.size() != files.size()) {
            throw new IllegalStateException("La carpeta '" + carpeta.getFileName() + "' tiene " + archivosReales.size()
                    + " archivos pero el metadata declara " + files.size());
        }

        for (int i = 0; i < files.size(); i++) {
            MangaMetadataFileDto file = files.get(i);
            MangaPage pagina = new MangaPage();
            pagina.setManga(manga);
            pagina.setNombreArchivo(archivosReales.get(i).getFileName().toString());
            pagina.setOrden(i + 1);
            pagina.setAncho(file.width());
            pagina.setAlto(file.height());
            pagina.setHash(file.hash());
            manga.getPaginas().add(pagina);
        }
    }

    private void agregarTags(Manga manga, List<MangaMetadataTagDto> tags) {
        if (tags == null) return;

        for (MangaMetadataTagDto tagDto : tags) {
            Tag tag = tagRepository.findByName(tagDto.tag()).orElseGet(() -> {
                Tag nuevo = new Tag();
                nuevo.setName(tagDto.tag());
                return tagRepository.save(nuevo);
            });

            MangaTag mangaTag = new MangaTag();
            mangaTag.setManga(manga);
            mangaTag.setTag(tag);
            mangaTag.setEsMale(Integer.valueOf(1).equals(tagDto.male()));
            mangaTag.setEsFemale(Integer.valueOf(1).equals(tagDto.female()));
            manga.getMangaTags().add(mangaTag);
        }
    }

    private void agregarMetadataValues(Manga manga, TipoMetadatoManga tipo, List<String> valores) {
        if (valores == null) return;

        for (String valor : valores) {
            if (valor == null || valor.isBlank()) continue;

            MangaMetadataValue metadataValue = mangaMetadataValueRepository.findByTipoAndValor(tipo, valor)
                    .orElseGet(() -> {
                        MangaMetadataValue nuevo = new MangaMetadataValue();
                        nuevo.setTipo(tipo);
                        nuevo.setValor(valor);
                        return mangaMetadataValueRepository.save(nuevo);
                    });

            manga.getMetadataValues().add(metadataValue);
        }
    }

    private TipoManga mapTipo(String type) {
        if (type == null) return null;

        return switch (type.toLowerCase()) {
            case "manga" -> TipoManga.MANGA;
            case "doujinshi" -> TipoManga.DOUJINSHI;
            case "artistcg" -> TipoManga.ARTIST_CG;
            case "gamecg" -> TipoManga.GAME_CG;
            case "imageset" -> TipoManga.IMAGE_SET;
            case "cosplay" -> TipoManga.COSPLAY;
            default -> {
                log.warn("Tipo de manga desconocido: '{}'", type);
                yield null;
            }
        };
    }

    private LocalDateTime parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) return null;

        try {
            return OffsetDateTime.parse(fecha, FECHA_METADATA_FORMAT).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.warn("No se pudo parsear la fecha '{}'", fecha, e);
            return null;
        }
    }
}
