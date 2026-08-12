package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.Tag;
import com.video.backend.video_backend.util.TagType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByNameAndType(String name, TagType type);

    List<Tag> findByNameInAndType(List<String> names, TagType type);

    Page<Tag> findByType(TagType type, Pageable pageable);

    @Query("SELECT t.name FROM Tag t WHERE t.type = :tipo AND LOWER(t.name) LIKE LOWER(CONCAT(:prefijo, '%')) ORDER BY t.name")
    List<String> findNamesStartingWith(@Param("prefijo") String prefijo, @Param("tipo") TagType tipo, Pageable pageable);
}
