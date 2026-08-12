package com.video.backend.video_backend.excepcion;

public class InvalidTagTypeException extends RuntimeException {
    public InvalidTagTypeException(String message) {
        super(message);
    }
}
