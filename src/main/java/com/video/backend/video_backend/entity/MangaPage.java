package com.video.backend.video_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manga_page", uniqueConstraints = @UniqueConstraint(columnNames = {"manga_id", "orden"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MangaPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manga_id", nullable = false)
    private Manga manga;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "ancho")
    private Integer ancho;

    @Column(name = "alto")
    private Integer alto;

    @Column(name = "hash")
    private String hash;
}
