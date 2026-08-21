package com.video.backend.video_backend.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
public class MangaIncomingScanService {

    private final ObjectMapper objectMapper;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    @Value("${app.manga.min-age-ms:60000}")
    private long minAgeMs;

    private static final String METADATA_FILE_NAME = "metadata.json";

    @Scheduled(fixedDelayString = "${app.manga.scan-interval-ms:300000}")
    public void scanIncomingFolder() {
        Path incomingDir = Paths.get(mediaBasePath, "mangaIncoming");
        Path libraryDir = Paths.get(mediaBasePath, "mangaLibrary");

        try {
            Files.createDirectories(incomingDir);
            Files.createDirectories(libraryDir);
        } catch (IOException e) {
            log.error("No se pudo crear los directorios de mangaIncoming/mangaLibrary", e);
            return;
        }

        AtomicInteger movidos = new AtomicInteger();
        AtomicInteger fallidos = new AtomicInteger();

        try (Stream<Path> entries = Files.list(incomingDir)) {
            entries.filter(Files::isDirectory)
                    .forEach(carpeta -> procesarCarpeta(carpeta, libraryDir, movidos, fallidos));
        } catch (IOException e) {
            log.error("No se pudo listar el contenido de mangaIncoming", e);
            return;
        }

        if (movidos.get() == 0 && fallidos.get() == 0) {
            return;
        }

        if (fallidos.get() == 0) {
            log.warn("Todos los mangas de mangaIncoming pudieron pasarse a mangaLibrary ({} movidos)", movidos.get());
        } else {
            log.warn("No todos los mangas de mangaIncoming pudieron pasarse a mangaLibrary: {} movidos, {} fallidos",
                    movidos.get(), fallidos.get());
        }
    }

    private void procesarCarpeta(Path carpeta, Path libraryDir, AtomicInteger movidos, AtomicInteger fallidos) {
        Path metadataPath = carpeta.resolve(METADATA_FILE_NAME);

        if (!Files.exists(metadataPath)) {
            log.warn("Carpeta '{}' sin {}, se omite", carpeta.getFileName(), METADATA_FILE_NAME);
            return;
        }

        if (!esCarpetaCompleta(carpeta, metadataPath)) {
            return;
        }

        Path destino = libraryDir.resolve(carpeta.getFileName());
        if (Files.exists(destino)) {
            log.warn("Ya existe una carpeta '{}' en mangaLibrary, se omite para no sobreescribir", carpeta.getFileName());
            return;
        }

        try {
            moverCarpeta(carpeta, destino);
            log.info("Manga '{}' movido de mangaIncoming a mangaLibrary", carpeta.getFileName());
            movidos.incrementAndGet();
        } catch (IOException e) {
            log.error("No se pudo mover la carpeta '{}' a mangaLibrary", carpeta.getFileName(), e);
            fallidos.incrementAndGet();
        }
    }

    private boolean esCarpetaCompleta(Path carpeta, Path metadataPath) {
        JsonNode metadata;
        try {
            metadata = objectMapper.readTree(metadataPath);
        } catch (JacksonException e) {
            log.warn("No se pudo leer {} en '{}', se omite", METADATA_FILE_NAME, carpeta.getFileName(), e);
            return false;
        }

        JsonNode files = metadata.get("files");
        if (files == null || !files.isArray() || files.isEmpty()) {
            log.warn("Carpeta '{}' tiene un {} sin arreglo 'files' valido, se omite", carpeta.getFileName(), METADATA_FILE_NAME);
            return false;
        }

        long archivosEnCarpeta;
        try (Stream<Path> entries = Files.list(carpeta)) {
            archivosEnCarpeta = entries
                    .filter(Files::isRegularFile)
                    .filter(archivo -> !archivo.getFileName().toString().equals(METADATA_FILE_NAME))
                    .count();
        } catch (IOException e) {
            log.warn("No se pudo listar el contenido de '{}', se omite", carpeta.getFileName(), e);
            return false;
        }

        if (archivosEnCarpeta != files.size()) {
            log.warn("Carpeta '{}' incompleta (se esperaban {} archivos, hay {}), se omite en este ciclo",
                    carpeta.getFileName(), files.size(), archivosEnCarpeta);
            return false;
        }

        return sinCambiosRecientes(carpeta);
    }

    private boolean sinCambiosRecientes(Path carpeta) {
        try (Stream<Path> entries = Files.walk(carpeta)) {
            Instant limite = Instant.now().minusMillis(minAgeMs);
            boolean modificadoRecientemente = entries
                    .map(this::lastModifiedTime)
                    .anyMatch(instante -> instante.isAfter(limite));

            if (modificadoRecientemente) {
                log.debug("Carpeta '{}' modificada muy recientemente, se espera al siguiente ciclo", carpeta.getFileName());
            }
            return !modificadoRecientemente;
        } catch (IOException e) {
            log.warn("No se pudo verificar la antiguedad de '{}', se omite", carpeta.getFileName(), e);
            return false;
        }
    }

    private Instant lastModifiedTime(Path path) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            return fileTime.toInstant();
        } catch (IOException e) {
            return Instant.now();
        }
    }

    private void moverCarpeta(Path origen, Path destino) throws IOException {
        try {
            Files.move(origen, destino, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origen, destino);
        }
    }
}
