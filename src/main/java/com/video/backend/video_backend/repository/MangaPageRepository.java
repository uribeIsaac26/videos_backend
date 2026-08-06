package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.MangaPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MangaPageRepository extends JpaRepository<MangaPage, Integer> {
    @Query("SELECT p FROM MangaPage p JOIN FETCH p.manga WHERE p.manga.id = :mangaId AND p.orden = :orden")
    Optional<MangaPage> findByMangaIdAndOrden(@Param("mangaId") Integer mangaId, @Param("orden") Integer orden);

    Page<MangaPage> findByMangaIdOrderByOrden(Integer mangaId, Pageable pageable);
}
