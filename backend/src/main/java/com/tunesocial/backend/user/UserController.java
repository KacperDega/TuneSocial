package com.tunesocial.backend.user;


import com.tunesocial.backend.user.dto.CreateUserRequest;
import com.tunesocial.backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
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
}

