package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {
    @Query("SELECT DISTINCT v FROM Video v LEFT JOIN FETCH v.tags")
    Page<Video> findAllWithTags(Pageable pageable);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.tags")
    Page<Video> findAll(Pageable pageable);

    @Query("SELECT v FROM Video v JOIN v.tags t WHERE t.id = :tagId")
    Page<Video> findByTagId(@Param("tagId") Integer tagId, Pageable pageable);
}
