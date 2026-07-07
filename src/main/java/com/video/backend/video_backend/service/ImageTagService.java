package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.ImageModel;
import com.video.backend.video_backend.dto.ImageTagRequest;
import com.video.backend.video_backend.entity.Image;
import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.excepcion.ImageNotFoundException;
import com.video.backend.video_backend.mapper.ImageMapper;
import com.video.backend.video_backend.repository.ImageRepository;
import com.video.backend.video_backend.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageTagService {
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final ImageMapper imageMapper;

    @Transactional
    public ImageModel addTagsToImage(ImageTagRequest imageTagRequest) {
        Image image = imageRepository.findById(imageTagRequest.getImageId()).orElseThrow(ImageNotFoundException::new);

        List<Tag> tags = tagRepository.findAllById(imageTagRequest.getTagIds());

        image.getTags().clear();
        image.getTags().addAll(tags);

        return imageMapper.toModel(image);
    }
}
