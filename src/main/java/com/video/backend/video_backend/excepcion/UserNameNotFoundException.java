package com.video.backend.video_backend.excepcion;

public class UserNameNotFoundException extends RuntimeException {
    public UserNameNotFoundException(String userName) {
        super("Usuario no encontrado " + userName);
    }
}
