package com.video.backend.video_backend.excepcion;

public class ImageNotFoundException extends RuntimeException{
    public ImageNotFoundException(){
        super("Imagen no encontrada");
    }
}
