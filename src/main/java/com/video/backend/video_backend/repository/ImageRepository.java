package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    @Query("SELECT i FROM Image i LEFT JOIN FETCH i.tags")
    Page<Image> findAll(Pageable pageable);

    @Query("SELECT i FROM Image i " +
            "JOIN i.tags t " +
            "WHERE t.id IN :tagIds " +
            "GROUP BY i.id " +
            "HAVING COUNT(DISTINCT t.id) = :tagCount")
    Page<Image> findByAllTags(
            @Param("tagIds") List<Integer> tagIds,
            @Param("tagCount") Long tagCount,
            Pageable pageable
    );
}
