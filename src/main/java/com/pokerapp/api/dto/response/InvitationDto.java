// src/main/java/com/pokerapp/api/dto/response/InvitationDto.java
package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private Long tableId;
    private String status;
    private String message;
    private String createdAt;
    private String expiresAt;
}
