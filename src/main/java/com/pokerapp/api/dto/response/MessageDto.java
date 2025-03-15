// src/main/java/com/pokerapp/api/dto/response/MessageDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

@Data
public class MessageDto {
    private String type; // INFO, ERROR, NOTIFICATION
    private String content;
    private Long playerId;
    private String timestamp;
}
