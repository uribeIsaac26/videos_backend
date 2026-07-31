package com.video.backend.video_backend.excepcion;

public class MangaNotFoundException extends RuntimeException {
    public MangaNotFoundException() {
        super("Manga no encontrado");
    }
}
