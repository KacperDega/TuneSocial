package com.tunesocial.backend.user.service;

import com.tunesocial.backend.user.dto.UpdateProfileRequest;
import com.tunesocial.backend.user.dto.UserProfileResponse;
import com.tunesocial.backend.user.mapper.UserMapper;
import com.tunesocial.backend.user.model.User;
import com.tunesocial.backend.user.model.UserProfile;
import com.tunesocial.backend.user.repository.UserProfileRepository;
import com.tunesocial.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // TODO: EXCEPTIONS

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));

        UserProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + username));

        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        userMapper.updateProfileFromDto(request, profile);

        UserProfile updatedProfile = profileRepository.save(profile);
        log.info("User profile updated successfully for userId: {}", userId);

        return UserProfileResponse.fromEntity(updatedProfile);
    }
}
