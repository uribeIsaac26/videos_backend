package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.VideoTagTemporalResponse;
import com.video.backend.video_backend.service.VideoTagTemporalService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/video-tag-temporal")
@RequiredArgsConstructor
public class VideoTagTemporalController {
    private final VideoTagTemporalService service;

    @GetMapping("/pending")
    public ResponseEntity<Page<VideoTagTemporalResponse>> findAllPending(Pageable pageable){
        return ResponseEntity.ok(service.findAllByConfirmFalse(pageable));
    }

}
