package com.video.backend.video_backend.excepcion;

public class MangaPageNotFoundException extends RuntimeException {
    public MangaPageNotFoundException() {
        super("Pagina de manga no encontrada");
    }
}
