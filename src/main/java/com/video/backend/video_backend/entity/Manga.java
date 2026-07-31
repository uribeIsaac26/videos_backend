package com.video.backend.video_backend.entity;

import com.video.backend.video_backend.util.TipoManga;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "manga", uniqueConstraints = @UniqueConstraint(columnNames = "source_id"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Manga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "japanese_title")
    private String japaneseTitle;

    @Column(name = "language")
    private String language;

    @Column(name = "language_local_name")
    private String languageLocalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoManga tipo;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "carpeta", nullable = false)
    private String carpeta;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MangaPage> paginas = new ArrayList<>();

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MangaTag> mangaTags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "manga_metadata_value_rel",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "metadata_value_id")
    )
    private Set<MangaMetadataValue> metadataValues = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
