package com.video.backend.video_backend.repository;

import com.video.backend.video_backend.entity.MangaMetadataValue;
import com.video.backend.video_backend.util.TipoMetadatoManga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MangaMetadataValueRepository extends JpaRepository<MangaMetadataValue, Integer> {
    Optional<MangaMetadataValue> findByTipoAndValor(TipoMetadatoManga tipo, String valor);
}
