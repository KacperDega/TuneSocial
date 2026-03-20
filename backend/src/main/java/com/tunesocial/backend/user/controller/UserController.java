package com.tunesocial.backend.user.controller;


import com.tunesocial.backend.user.mapper.UserMapper;
import com.tunesocial.backend.user.service.UserService;
import com.tunesocial.backend.user.dto.CreateUserRequest;
import com.tunesocial.backend.user.dto.UserResponse;
import com.tunesocial.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping
    public UserResponse create(@RequestBody CreateUserRequest req) {
        User createdUser = userService.create(req);
        return userMapper.toUserResponse(createdUser);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll().stream().map(userMapper::toUserResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        User foundUser = userService.getById(id);
        return userMapper.toUserResponse(foundUser);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody CreateUserRequest req) {
        User user = userService.update(id, req);
        return userMapper.toUserResponse(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/me/me")
    public UserResponse me(Authentication auth) {
        User currentUser = (User) auth.getPrincipal();
        log.info("Current User: {}", currentUser.toString());
        log.info("Current User ID: {}", currentUser.getId());

        return userMapper.toUserResponse(currentUser);
    }

}

