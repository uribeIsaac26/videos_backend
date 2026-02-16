package com.video.backend.video_backend.dto;

public record LoginRequest(
        String username,
        String password
) {}
