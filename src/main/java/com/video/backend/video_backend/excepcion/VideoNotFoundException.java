package com.video.backend.video_backend.excepcion;

public class VideoNotFoundException extends RuntimeException{
    public VideoNotFoundException(){
        super("Video no encontrado");
    }
}
