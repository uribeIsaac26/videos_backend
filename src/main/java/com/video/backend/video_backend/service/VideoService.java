package com.video.backend.video_backend.service;

import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.excepcion.ThumbnailNotFoundException;
import com.video.backend.video_backend.excepcion.VideoNotFoundException;
import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.model.VideoModel;
import com.video.backend.video_backend.model.VideoStream;
import com.video.backend.video_backend.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
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
}
