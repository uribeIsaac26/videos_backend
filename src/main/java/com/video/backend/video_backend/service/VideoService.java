package com.video.backend.video_backend.service;

import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.excepcion.ThumbnailNotFoundException;
import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.model.VideoModel;
import com.video.backend.video_backend.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    public ResponseEntity<List<VideoModel>> findAll(){
        List<VideoModel> videoModels =  videoRepository.findAll().stream().map(videoMapper::toModel).toList();

        return ResponseEntity.ok(videoModels);
    }

    public Void findThumbnailByIdVideo(Integer id){
        Optional<Video> videoModelOptional = videoRepository.findById(id);
        if (videoModelOptional.isEmpty()){
            throw new ThumbnailNotFoundException(id);
        }

        return null;
    }
}
