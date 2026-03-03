package com.video.backend.video_backend.controller;

import com.video.backend.video_backend.dto.LoginRequest;
import com.video.backend.video_backend.dto.LoginResponse;
import com.video.backend.video_backend.dto.RegisterRequest;
import com.video.backend.video_backend.security.JwtService;
import com.video.backend.video_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.hibernate.SpringImplicitNamingStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(request.username());

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofHours(5))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request){
        authService.register(request);
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication authentication) {
        System.out.println("Auth in controller: " + authentication);
        return ResponseEntity.ok(authentication.getName());
    }
}
