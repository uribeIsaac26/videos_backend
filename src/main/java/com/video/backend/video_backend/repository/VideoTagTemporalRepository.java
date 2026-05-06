package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.VideoTagTemporal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoTagTemporalRepository extends JpaRepository<VideoTagTemporal, Integer> {
    Page<VideoTagTemporal> findByConfirmFalse(Pageable pageable);
}
