package com.video.backend.video_backend.service;

import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.model.VideoModel;
import com.video.backend.video_backend.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    public ResponseEntity<List<VideoModel>> findAll(){
        List<VideoModel> videoModels =  videoRepository.findAll().stream().map(videoMapper::toModel).toList();

        return ResponseEntity.ok(videoModels);
    }
}
