package com.video.backend.video_backend.excepcion;

public class TagAlreadyExistsException extends RuntimeException{
    public TagAlreadyExistsException(){
        super("El tag ya existe");
    }
}
