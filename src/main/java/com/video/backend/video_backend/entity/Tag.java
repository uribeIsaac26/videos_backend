package com.video.backend.video_backend.entity;

import com.video.backend.video_backend.util.TagType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    // columnDefinition incluye DEFAULT 'VIDEO' para que el ALTER TABLE no falle sobre
    // filas existentes (creadas antes de separar tags por tipo de contenido); TagTypeMigrationRunner
    // corrige el valor real de esas filas legacy al arrancar.
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'VIDEO'")
    private TagType type;

    @ManyToMany(mappedBy = "tags")
    private Set<Video> videoSet = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Image> imageSet = new HashSet<>();

    @OneToMany(mappedBy = "tag")
    private Set<MangaTag> mangaTagSet = new HashSet<>();
}
