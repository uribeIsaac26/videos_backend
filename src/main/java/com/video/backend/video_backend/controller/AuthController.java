package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.LoginRequest;
import com.video.backend.video_backend.dto.LoginResponse;
import com.video.backend.video_backend.dto.RegisterRequest;
import com.video.backend.video_backend.security.JwtService;
import com.video.backend.video_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.hibernate.SpringImplicitNamingStrategy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(request.username());

        return new LoginResponse(token);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request){
        authService.register(request);
    }
}
