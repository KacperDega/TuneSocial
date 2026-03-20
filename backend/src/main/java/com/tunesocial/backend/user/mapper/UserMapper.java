package com.tunesocial.backend.user.mapper;

import com.tunesocial.backend.user.dto.UpdateProfileRequest;
import com.tunesocial.backend.user.dto.UserResponse;
import com.tunesocial.backend.user.model.User;
import com.tunesocial.backend.user.model.UserProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    void updateProfileFromDto(UpdateProfileRequest dto, @MappingTarget UserProfile entity);
}
