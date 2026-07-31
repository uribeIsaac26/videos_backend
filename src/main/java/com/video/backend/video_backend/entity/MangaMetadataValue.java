package com.video.backend.video_backend.entity;

import com.video.backend.video_backend.util.TipoMetadatoManga;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manga_metadata_value", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "valor"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MangaMetadataValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMetadatoManga tipo;

    @Column(name = "valor", nullable = false)
    private String valor;
}
