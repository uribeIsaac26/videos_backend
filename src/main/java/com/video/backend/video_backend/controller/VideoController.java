package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.model.VideoModel;
import com.video.backend.video_backend.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @GetMapping
    public List<VideoModel> findAll(){
        return videoService.findAll();
    }
}
