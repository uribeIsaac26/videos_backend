package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.TagRequest;
import com.video.backend.video_backend.dto.TagResponse;
import com.video.backend.video_backend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Page<TagResponse>> findAll(Pageable pageable){
        return ResponseEntity.ok(tagService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<TagResponse> save(@RequestBody TagRequest tagRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.save(tagRequest));
    }
}
