package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.VideoRequest;
import com.video.backend.video_backend.dto.VideoStatusResponse;
import com.video.backend.video_backend.dto.VideoStream;
import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.excepcion.ThumbnailNotFoundException;
import com.video.backend.video_backend.excepcion.VideoNotFoundException;
import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.dto.VideoModel;
import com.video.backend.video_backend.repository.VideoRepository;
import com.video.backend.video_backend.util.VideoStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    private final VideoTranscodingService videoTranscodingService;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    private static final long CHUNK_SIZE = 1024 * 1024;

    @Transactional
    public Page<VideoModel> findAll(Pageable pageable){
        Page<Video> page = videoRepository.findAll(pageable);

        List<Integer> ids = page.getContent().stream().map(Video::getId).toList();
        Map<Integer, Video> withTagsById = videoRepository.findAllWithTagsByIdIn(ids).stream()
                .collect(Collectors.toMap(Video::getId, Function.identity()));

        List<VideoModel> models = ids.stream()
                .map(withTagsById::get)
                .map(videoMapper::toModel)
                .toList();

        return new PageImpl<>(models, pageable, page.getTotalElements());
    }

    public Resource findThumbnailByIdVideo(Integer id){
        Video video = videoRepository.findById(id).orElseThrow(()-> new ThumbnailNotFoundException(id));
        Path thumbnailPath = Paths.get(mediaBasePath)
                .resolve(video.getThumbnailPath())
                .normalize();
        try {
            Resource resource = new UrlResource(thumbnailPath.toUri());

            if (!resource.exists() || !resource.isReadable()){
                throw new ThumbnailNotFoundException(id);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public VideoStream findVideoById(Integer id, HttpHeaders headers) throws IOException {
        Path videoPath = getVideoPath(id);

        UrlResource videoResource = new UrlResource(videoPath.toUri());

        long contentLength = videoResource.contentLength();

        List<HttpRange> ranges = headers.getRange();
        ResourceRegion region;

        if (ranges.isEmpty()){
            long rangeLenght = Math.min(CHUNK_SIZE, contentLength);
            region = new ResourceRegion(videoResource, 0, rangeLenght);
        }else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(CHUNK_SIZE, end -start + 1);
            region = new ResourceRegion(videoResource, start, rangeLength);
        }

        MediaType mediaType = MediaTypeFactory
                .getMediaType(videoResource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return new VideoStream(region, mediaType);
    }

    public Path getVideoPath(Integer id){
        Video video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);

        Path videoPath = Paths.get(mediaBasePath)
                .resolve(video.getVideoPath())
                .normalize();

        if (!Files.exists(videoPath)) {
            throw new VideoNotFoundException();
        }

        return videoPath;
    }

    @Transactional
    public void deleteVideo(Integer id) throws IOException {
        Video video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);

        Path videoPath = Paths.get(mediaBasePath, video.getVideoPath());
        Files.deleteIfExists(videoPath);

        Path thumbnailPath = Paths.get(mediaBasePath, video.getThumbnailPath());
        Files.deleteIfExists(thumbnailPath);

        log.info("Video eliminado");

        videoRepository.deleteById(id);
    }

    public VideoModel uploadVideo(String title, MultipartFile videoFile, MultipartFile thumbnailFile) {
        videoValidations(videoFile);

        String videoId = UUID.randomUUID().toString();

        String originalFilename = videoFile.getOriginalFilename();
        String originalExtension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".tmp";

        String videoFileName = videoId + ".mp4";
        String thumbnailFileName = videoId + ".jpg";

        Path videoDirectory = Paths.get(mediaBasePath, "video");
        Path videoPath = videoDirectory.resolve(videoFileName).normalize();
        Path tempPath = videoDirectory.resolve(videoId + "_original" + originalExtension).normalize();
        Path thumbnailDirectory = Paths.get(mediaBasePath, "thumbnails");
        Path thumbnailPath = thumbnailDirectory.resolve(thumbnailFileName).normalize();

        try {
            Files.createDirectories(videoDirectory);
            Files.createDirectories(thumbnailDirectory);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear los directorios de media", e);
        }

        try (InputStream inputStream = videoFile.getInputStream()) {
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo del video", e);
        }

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try (InputStream inputStream = thumbnailFile.getInputStream()) {
                Files.copy(inputStream, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error al guardar el thumbnail", e);
            }
        } else {
            generateThumbnail(tempPath, thumbnailPath);
        }

        VideoRequest videoRequest = VideoRequest.builder()
                .title(title)
                .videoFileName(videoFileName)
                .thumbnailFileName(thumbnailFileName)
                .size(videoFile.getSize())
                .build();

        VideoModel model = saveVideo(videoRequest, videoPath, thumbnailPath);

        try {
            videoTranscodingService.transcode(tempPath, videoPath, model.getId());
        } catch (TaskRejectedException e) {
            log.error("[video={}] No se pudo encolar la transcodificacion, cola llena", model.getId(), e);
            videoTranscodingService.markRejected(model.getId());
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ex) {
                log.warn("[video={}] No se pudo eliminar el archivo temporal tras el rechazo: {}", model.getId(), tempPath);
            }
            throw new RuntimeException("El servidor esta procesando muchos videos en este momento, intente nuevamente en unos minutos", e);
        }

        return model;
    }

    public VideoStatusResponse getVideoStatus(Integer id) {
        Video video = videoRepository.findById(id).orElseThrow(VideoNotFoundException::new);
        return new VideoStatusResponse(video.getId(), video.getStatus(), video.getErrorMessage());
    }

    private void generateThumbnail(Path sourcePath, Path thumbnailPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", sourcePath.toAbsolutePath().toString(),
                    "-ss", "00:00:02",
                    "-vframes", "1",
                    "-q:v", "2",
                    thumbnailPath.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> log.debug("ffmpeg thumbnail: {}", line));
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("ffmpeg timeout al generar el thumbnail");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error generando thumbnail", e);
        }
    }

    private void videoValidations(MultipartFile videoFile){
        if (videoFile == null || videoFile.isEmpty()){
            throw new IllegalArgumentException("El Archivo de video es obligatorio");
        }

        String contentType = videoFile.getContentType();
        if(contentType == null || !contentType.startsWith("video/")){
            throw new IllegalArgumentException("El archivo debe ser un video valido");
        }
    }

    private VideoModel saveVideo(VideoRequest videoRequest, Path videoPath, Path thumbnailPath) {
        try {
            Video entity = videoMapper.toEntity(videoRequest);
            entity.setStatus(VideoStatus.PROCESANDO);
            Video video = videoRepository.save(entity);
            return videoMapper.toModel(video);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(videoPath);
                Files.deleteIfExists(thumbnailPath);
            } catch (IOException ex) {
                log.error("Error guardando video en db, no se pudieron borrar los archivos");
            }
            throw new RuntimeException("Error guardando video en la db", e);
        }
    }

    public Page<VideoModel> findByAllTags(List<Integer> tagIds, Pageable pageable) {
        Long tagCount = (long) tagIds.size();
        return videoRepository.findByAllTags(tagIds, tagCount, pageable)
                .map(videoMapper::toModel);
    }

}
