package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(List<String> names);

    @Query("SELECT t.name FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefijo, '%')) ORDER BY t.name")
    List<String> findNamesStartingWith(@Param("prefijo") String prefijo, Pageable pageable);
}
