package com.tunesocial.backend.user.service;

import com.tunesocial.backend.user.dto.CreateUserRequest;
import com.tunesocial.backend.user.dto.UserRefDto;
import com.tunesocial.backend.user.model.User;
import com.tunesocial.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    public User getCurrentUser(Authentication authentication) {
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new IllegalStateException("No authenticated user");
//        }
//
//        Object principal = authentication.getPrincipal();
//
//        return (User) principal;
//    }

    public Long getCurrentUserIdOrThrow(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        User principal = (User) authentication.getPrincipal();

        return principal.getId();
    }

    public Long getCurrentUserIdOrNull(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        User principal = (User) authentication.getPrincipal();

        return principal.getId();
    }


    public Map<Long, UserRefDto> getUserReferencesByIds(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();

        return userRepository.findUserRefsByIds(userIds).stream()
                .collect(Collectors.toMap(UserRefDto::userId, userRef -> userRef));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User create(CreateUserRequest req) {
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        return userRepository.save(user);
    }

    public User update(Long id, CreateUserRequest req) {
        User user = getById(id);
        user.setUsername(req.username());
        user.setEmail(req.email());

        if (req.password() != null && !req.password().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}

