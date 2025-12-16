package com.tunesocial.backend.auth;

import com.tunesocial.backend.auth.dto.AuthResponse;
import com.tunesocial.backend.auth.dto.LoginRequest;
import com.tunesocial.backend.auth.dto.RegisterRequest;
import com.tunesocial.backend.auth.exception.EmailAlreadyExistsException;
import com.tunesocial.backend.auth.exception.InvalidCredentialsException;
import com.tunesocial.backend.auth.exception.UsernameAlreadyExistsException;
import com.tunesocial.backend.common.security.jwt.JwtService;
import com.tunesocial.backend.user.User;
import com.tunesocial.backend.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId());

        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {

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

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token);
    }

}

