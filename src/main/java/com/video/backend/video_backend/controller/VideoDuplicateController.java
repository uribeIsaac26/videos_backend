package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.VideoDuplicateGroupDetailResponse;
import com.video.backend.video_backend.dto.VideoDuplicateGroupSummaryResponse;
import com.video.backend.video_backend.service.VideoDuplicateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/video-duplicates")
@RequiredArgsConstructor
public class VideoDuplicateController {

    private final VideoDuplicateService service;

    @GetMapping
    public ResponseEntity<Page<VideoDuplicateGroupSummaryResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDuplicateGroupDetailResponse> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
