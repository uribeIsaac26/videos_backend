package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {

}
