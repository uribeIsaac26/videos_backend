package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.VideoRequest;
import com.video.backend.video_backend.dto.VideoStream;
import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.excepcion.ThumbnailNotFoundException;
import com.video.backend.video_backend.excepcion.VideoNotFoundException;
import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.dto.VideoModel;
import com.video.backend.video_backend.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    public List<VideoModel> findAll(){
        List<VideoModel> videoModels =  videoRepository.findAll().stream().map(videoMapper::toModel).toList();

        return videoModels;
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
            region = new ResourceRegion(videoResource, 0, contentLength);
        }else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end -start + 1);
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

    public VideoModel uploadVideo(String title, MultipartFile videoFile, MultipartFile thumbnailFile){
        videoValidations(videoFile);

        String videoId = UUID.randomUUID().toString();

        String videoFileName = videoId + ".mp4";
        String thumbnailFileName = videoId + ".jpg";

        Path videoDirectory = Paths.get(mediaBasePath, "video");
        Path videoPath = videoDirectory.resolve(videoFileName).normalize();

        try {
            Files.createDirectories(videoDirectory);
        }catch (IOException e){
            throw new RuntimeException("No se pudo crear el directorio de videos", e);
        }

        try(InputStream inputStream = videoFile.getInputStream()){
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new RuntimeException("Error al guardar el archivo del video", e);
        }

        Path thumbnailDirectory = Paths.get(mediaBasePath, "thumbnails");
        Path thumbnailPath = thumbnailDirectory.resolve(thumbnailFileName).normalize();

        try {
            Files.createDirectories(thumbnailDirectory);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de thumbnails", e);
        }

        if (thumbnailFile != null && !thumbnailFile.isEmpty()){
            try (InputStream inputStream = thumbnailFile.getInputStream()){
                Files.copy(inputStream, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
            }catch (IOException e){
                throw new IllegalArgumentException("Error al guardar el thumbnail", e);
            }
        }else {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg",
                        "-i", videoPath.toAbsolutePath().toString(),
                        "-ss", "00:00:02",
                        "-vframes", "1",
                        "-q:v", "2",
                        thumbnailPath.toAbsolutePath().toString()
                );

                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                int exitCode = process.waitFor();

                if (exitCode != 0){
                    throw new RuntimeException("Error al generar thumbnail con ffmpeg");
                }
            }catch (IOException | InterruptedException e){
                throw new RuntimeException("Error ejeccutando ffmpeg");
            }
        }

        VideoRequest videoRequest = VideoRequest.builder()
                .title(title)
                .videoFileName(videoFileName)
                .thumbnailFileName(thumbnailFileName)
                .size(videoFile.getSize())
                .build();

        return saveVideo(videoRequest, videoPath, thumbnailPath);

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

    private VideoModel saveVideo(VideoRequest videoRequest, Path videoPath, Path thumbnailPath){

        Video video = new Video();

        try {
            video = videoRepository.save(videoMapper.toEntity(videoRequest));
        }catch (Exception e){
            try{
                Files.deleteIfExists(videoPath);
                Files.deleteIfExists(thumbnailPath);
            }catch (IOException ex){
                log.error("Ocurrio un error en el guardado del video, se borrar archivo guardados");
            }
            throw new RuntimeException("Error guardando video en la db", e);
        }

        return videoMapper.toModel(video);
    }
}
