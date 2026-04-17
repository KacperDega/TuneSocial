package com.tunesocial.backend.relation.mapper;

import com.tunesocial.backend.relation.dto.FriendRequestDto;
import com.tunesocial.backend.relation.model.FriendRequest;
import com.tunesocial.backend.user.dto.UserRefDto;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FriendRequestMapper {

    public FriendRequestDto toFriendRequestDto(
            FriendRequest request,
            Map<Long, UserRefDto> requestersDetails
    ) {
        return new FriendRequestDto(
                request.getId(),
                requestersDetails.getOrDefault(
                        request.getRequesterId(),
                        new UserRefDto(request.getRequesterId(), null, "User_" + request.getRequesterId(), 1)
                ),
                request.getRecipientId(),
                request.getCreatedAt()
        );
    }

    public List<FriendRequestDto> toDtoList(
            List<FriendRequest> requests,
            Map<Long, UserRefDto> requestersDetails
    ) {
        return requests.stream()
                .map(request -> toFriendRequestDto(request, requestersDetails))
                .toList();
    }
}
