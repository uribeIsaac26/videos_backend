package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.VideoModel;
import com.video.backend.video_backend.dto.VideoTagRequest;
import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.entity.Video;
import com.video.backend.video_backend.excepcion.VideoNotFoundException;
import com.video.backend.video_backend.mapper.VideoMapper;
import com.video.backend.video_backend.repository.TagRepository;
import com.video.backend.video_backend.repository.VideoRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoTagService {
    private final VideoRepository videoRepository;
    private final TagRepository tagRepository;
    private final VideoMapper videoMapper;

    @Transactional
    public VideoModel addTagsToVideo(VideoTagRequest videoTagRequest){
        Video video = videoRepository.findById(videoTagRequest.getVideoId()).orElseThrow(VideoNotFoundException::new);

        List<Tag> tags = tagRepository.findAllById(videoTagRequest.getTagIds());

        video.getTags().clear();
        video.getTags().addAll(tags);

        return videoMapper.toModel(video);

    }
}
