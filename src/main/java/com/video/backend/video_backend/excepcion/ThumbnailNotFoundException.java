package com.video.backend.video_backend.excepcion;

public class ThumbnailNotFoundException extends RuntimeException{
    public ThumbnailNotFoundException(Integer id){
        super("El thumbnail para el video " + id + "no fue encontrado");
    }
}
