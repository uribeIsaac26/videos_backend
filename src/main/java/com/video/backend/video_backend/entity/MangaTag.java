package com.video.backend.video_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manga_tag", uniqueConstraints = @UniqueConstraint(columnNames = {"manga_id", "tag_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MangaTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manga_id", nullable = false)
    private Manga manga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "es_male")
    private Boolean esMale = false;

    @Column(name = "es_female")
    private Boolean esFemale = false;
}
