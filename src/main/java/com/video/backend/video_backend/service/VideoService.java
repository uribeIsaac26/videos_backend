package com.video.backend.video_backend.service;

import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.model.VideoModel;
import com.video.backend.video_backend.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    public List<VideoModel> findAll(){
        return videoRepository.findAll().stream().map(videoMapper::toModel).toList();
    }
}
