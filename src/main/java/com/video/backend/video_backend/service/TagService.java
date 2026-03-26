package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.TagRequest;
import com.video.backend.video_backend.dto.TagResponse;
import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.excepcion.TagAlreadyExistsException;
import com.video.backend.video_backend.mapper.TagMapper;
import com.video.backend.video_backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagResponse save(TagRequest tagRequest){

        if (tagRepository.findByName(tagRequest.name()).isPresent()) throw new TagAlreadyExistsException();

        return tagMapper.toResponse(tagRepository.save(tagMapper.toEntity(tagRequest)));
    }

    public Page<TagResponse> findAll(Pageable pageable){
        return tagRepository.findAll(pageable).map(tagMapper::toResponse);
    }


}
