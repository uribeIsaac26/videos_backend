package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.Manga;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Integer> {
    Optional<Manga> findBySourceId(Long sourceId);

    @Query("SELECT m FROM Manga m " +
            "JOIN m.mangaTags mt " +
            "WHERE mt.tag.id IN :tagIds " +
            "GROUP BY m.id " +
            "HAVING COUNT(DISTINCT mt.tag.id) = :tagCount")
    Page<Manga> findByAllTags(
            @Param("tagIds") List<Integer> tagIds,
            @Param("tagCount") Long tagCount,
            Pageable pageable
    );

    @Query("SELECT m FROM Manga m JOIN m.metadataValues v WHERE v.tipo = :tipo AND v.valor = :valor")
    Page<Manga> findByMetadataValue(
            @Param("tipo") TipoMetadatoManga tipo,
            @Param("valor") String valor,
            Pageable pageable
    );
}
