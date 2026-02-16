package com.video.backend.video_backend.service;

import com.video.backend.video_backend.dto.RegisterRequest;
import com.video.backend.video_backend.entity.User;
import com.video.backend.video_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request){
        if (userRepository.findByUserName(request.username()).isPresent()){
            throw new RuntimeException("Username ya existe");
        }

        User user = User.builder()
                .userName(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
    }
}
