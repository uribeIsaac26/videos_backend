package com.video.backend.video_backend.entity;

import com.video.backend.video_backend.util.Accion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_duplicate_member")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoDuplicateMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private VideoDuplicateGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "similitud", nullable = false)
    private Float similitud;

    @Column(name = "revisado")
    private Boolean revisado = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion")
    private Accion accion = Accion.PENDIENTE;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}
