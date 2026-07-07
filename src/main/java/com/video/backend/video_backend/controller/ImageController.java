package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.ImageModel;
import com.video.backend.video_backend.dto.ImageTagRequest;
import com.video.backend.video_backend.service.ImageService;
import com.video.backend.video_backend.service.ImageTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    private final ImageTagService imageTagService;

    @GetMapping
    public ResponseEntity<Page<ImageModel>> findAll(Pageable pageable) {
        return ResponseEntity.ok(imageService.findAll(pageable));
    }

    @GetMapping("{id}/image")
    public ResponseEntity<Resource> findImageById(@PathVariable("id") Integer id) throws IOException {
        Resource resource = imageService.findImageById(id);

        Path path = resource.getFile().toPath();
        String contentType = Files.probeContentType(path);

        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageModel> uploadImage(
            @RequestParam("title") String title,
            @RequestParam("imageFile") MultipartFile imageFile
    ) {
        ImageModel response = imageService.uploadImage(title, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer id) throws IOException {
        imageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tag")
    public ResponseEntity<ImageModel> addTagsToImage(@RequestBody ImageTagRequest imageTagRequest) {
        return ResponseEntity.ok(imageTagService.addTagsToImage(imageTagRequest));
    }

    @GetMapping("/tag")
    public ResponseEntity<Page<ImageModel>> getImagesByTag(
            @RequestParam List<Integer> tagIds,
            Pageable pageable) {
        return ResponseEntity.ok(imageService.findByAllTags(tagIds, pageable));
    }
}
