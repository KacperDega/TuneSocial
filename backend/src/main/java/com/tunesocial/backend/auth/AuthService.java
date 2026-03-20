package com.tunesocial.backend.auth;

import com.tunesocial.backend.auth.dto.*;
import com.tunesocial.backend.auth.exception.*;
import com.tunesocial.backend.common.security.jwt.JwtService;
import com.tunesocial.backend.user.repository.UserProfileRepository;
import com.tunesocial.backend.user.model.User;
import com.tunesocial.backend.user.repository.UserRepository;
import com.tunesocial.backend.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new UsernameAlreadyExistsException();
        }

        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setDisplayName(req.displayName());
        user.setPasswordHash(passwordEncoder.encode(req.password()));

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setAvatarId(1);
        userProfileRepository.save(profile);

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token);
    }
}