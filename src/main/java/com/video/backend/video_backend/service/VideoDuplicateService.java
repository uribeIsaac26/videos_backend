package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.VideoDuplicateGroupDetailResponse;
import com.video.backend.video_backend.dto.VideoDuplicateGroupSummaryResponse;
import com.video.backend.video_backend.entity.VideoDuplicateGroup;
import com.video.backend.video_backend.excepcion.VideoDuplicateGroupNotFoundException;
import com.video.backend.video_backend.mapper.VideoDuplicateMapper;
import com.video.backend.video_backend.repository.VideoDuplicateGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoDuplicateService {

    private final VideoDuplicateGroupRepository repository;
    private final VideoDuplicateMapper mapper;

    @Transactional(readOnly = true)
    public Page<VideoDuplicateGroupSummaryResponse> findAll(Pageable pageable) {
        return repository.findAllByResueltoFalse(pageable).map(mapper::toSummary);
    }

    @Transactional
    public void resolve(Integer id) {
        VideoDuplicateGroup group = repository.findById(id)
                .orElseThrow(VideoDuplicateGroupNotFoundException::new);
        group.setResuelto(true);
        repository.save(group);
    }

    @Transactional(readOnly = true)
    public VideoDuplicateGroupDetailResponse findById(Integer id) {
        VideoDuplicateGroup group = repository.findById(id)
                .orElseThrow(VideoDuplicateGroupNotFoundException::new);
        return mapper.toDetail(group);
    }
}
