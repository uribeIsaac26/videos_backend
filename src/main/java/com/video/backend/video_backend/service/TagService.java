package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.TagRequest;
import com.video.backend.video_backend.dto.TagResponse;
import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.excepcion.TagAlreadyExistsException;
import com.video.backend.video_backend.excepcion.TagNotFoundException;
import com.video.backend.video_backend.mapper.TagMapper;
import com.video.backend.video_backend.repository.TagRepository;
import com.video.backend.video_backend.util.TagType;
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

        if (tagRepository.findByNameAndType(tagRequest.name(), tagRequest.type()).isPresent()) throw new TagAlreadyExistsException();

        return tagMapper.toResponse(tagRepository.save(tagMapper.toEntity(tagRequest)));
    }

    public Page<TagResponse> findAll(TagType type, Pageable pageable){
        return tagRepository.findByType(type, pageable).map(tagMapper::toResponse);
    }

    public TagResponse findById(Integer id){
        return tagMapper.toResponse(tagRepository.findById(id).orElseThrow(TagNotFoundException::new));
    }

    public List<TagResponse> findByName(List<String> tags, TagType type){
        return tagRepository.findByNameInAndType(tags, type).stream().map(tagMapper::toResponse).toList();
    }

}
