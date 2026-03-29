package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.VideoModel;
import com.video.backend.video_backend.dto.VideoStream;
import com.video.backend.video_backend.dto.VideoTagRequest;
import com.video.backend.video_backend.service.VideoService;
import com.video.backend.video_backend.service.VideoTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("api/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;
    private final VideoTagService videoTagService;

    @GetMapping
    public ResponseEntity<Page<VideoModel>> findAll(Pageable pageable){
        return ResponseEntity.ok(videoService.findAll(pageable));
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
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(stream.getRegion());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoModel> uploadVideo(
            @RequestParam("title") String title,
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
            ){
        VideoModel response = videoService.uploadVideo(title, videoFile, thumbnailFile);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Integer id) throws IOException {
        videoService.deleteVideo(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tag")
    public ResponseEntity<VideoModel> addTagsTovideoo(@RequestBody VideoTagRequest videoTagRequest){
        VideoModel updatedVideo = videoTagService.addTagsToVideo(videoTagRequest);

        return ResponseEntity.ok(updatedVideo);
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<Page<VideoModel>> getVideosByTag(
            @PathVariable Integer tagId,
            Pageable pageable) {
        return ResponseEntity.ok(videoService.findByTag(tagId, pageable));
    }
}
