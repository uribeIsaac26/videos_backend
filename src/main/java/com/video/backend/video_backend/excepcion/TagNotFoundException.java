package com.video.backend.video_backend.excepcion;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException() {
        super("El tag no exise");
    }
}
