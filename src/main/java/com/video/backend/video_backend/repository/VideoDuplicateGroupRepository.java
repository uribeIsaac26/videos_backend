package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.VideoDuplicateGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoDuplicateGroupRepository extends JpaRepository<VideoDuplicateGroup, Integer> {
    Page<VideoDuplicateGroup> findAllByResueltoFalse(Pageable pageable);
}
