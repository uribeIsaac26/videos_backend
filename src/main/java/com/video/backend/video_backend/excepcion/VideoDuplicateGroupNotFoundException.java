package com.video.backend.video_backend.excepcion;

public class VideoDuplicateGroupNotFoundException extends RuntimeException {
    public VideoDuplicateGroupNotFoundException() {
        super("Grupo de duplicados no encontrado");
    }
}
