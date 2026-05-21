package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.VideoTagTemporalResponse;
import com.video.backend.video_backend.entity.VideoTagTemporal;
import com.video.backend.video_backend.excepcion.VideoNotFoundException;
import com.video.backend.video_backend.mapper.VideoTagTemporalMapper;
import com.video.backend.video_backend.repository.VideoTagTemporalRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
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

    public void updateVideoTemporalToConfirm(Integer idVideo){
        VideoTagTemporal entityExists = repository.findByVideoId(idVideo);
        if (entityExists == null) {
            return;
        }
        entityExists.setConfirm(true);
        repository.save(entityExists);
    }

}
