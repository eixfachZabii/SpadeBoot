// src/main/java/com/pokerapp/api/dto/response/FriendshipDto.java
package com.pokerapp.api.dto.response;

import com.pokerapp.domain.user.FriendshipStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FriendshipDto {
    private Long id;
    private Long requesterId;
    private String requesterUsername;
    private Long addresseeId;
    private String addresseeUsername;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}