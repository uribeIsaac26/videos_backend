package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.model.VideoModel;
import com.video.backend.video_backend.model.VideoStream;
import com.video.backend.video_backend.service.VideoService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("api/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<VideoModel>> findAll(){
        return ResponseEntity.ok(videoService.findAll());
    }

    @GetMapping("{id}/thumbnail")
    public ResponseEntity<Resource> findThumbnailByIdVideo(@PathVariable("id") Integer id) throws IOException {
        Resource resource = videoService.findThumbnailByIdVideo(id);

        Path path = resource.getFile().toPath();
        String contentType = Files.probeContentType(path);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("{id}/video")
    public ResponseEntity<ResourceRegion> findVideoById(@PathVariable Integer id, @RequestHeader HttpHeaders httpHeaders) throws IOException {
        VideoStream stream = videoService.findVideoById(id, httpHeaders);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(stream.getMediaType())
                .body(stream.getRegion());
    }
}
