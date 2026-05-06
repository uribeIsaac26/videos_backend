package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.VideoTagTemporalResponse;
import com.video.backend.video_backend.mapper.VideoTagTemporalMapper;
import com.video.backend.video_backend.repository.VideoTagTemporalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.Serial;

@Service
@RequiredArgsConstructor
public class VideoTagTemporalService {
    private final VideoTagTemporalRepository repository;
    private final VideoTagTemporalMapper mapper;

    public Page<VideoTagTemporalResponse> findAllByConfirmFalse(Pageable pageable){
        return repository.findByConfirmFalse(pageable).map(mapper::toDto);
    }

}
