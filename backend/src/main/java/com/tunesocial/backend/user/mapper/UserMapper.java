package com.tunesocial.backend.user.mapper;

import com.tunesocial.backend.user.dto.UserResponse;
import com.tunesocial.backend.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}
