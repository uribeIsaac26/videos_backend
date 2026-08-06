package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.MangaMetadataValue;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MangaMetadataValueRepository extends JpaRepository<MangaMetadataValue, Integer> {
    Optional<MangaMetadataValue> findByTipoAndValor(TipoMetadatoManga tipo, String valor);

    @Query("SELECT v.valor FROM MangaMetadataValue v " +
            "WHERE v.tipo = :tipo AND LOWER(v.valor) LIKE LOWER(CONCAT(:prefijo, '%')) " +
            "ORDER BY v.valor")
    List<String> findValoresStartingWith(
            @Param("tipo") TipoMetadatoManga tipo,
            @Param("prefijo") String prefijo,
            Pageable pageable
    );
}
