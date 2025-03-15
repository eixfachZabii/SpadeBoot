// src/main/java/com/pokerapp/api/dto/response/GameActionDto.java
package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameActionDto {
    private Long id;
    private Long playerId;
    private String playerName;
    private String actionType;
    private String actionData;
    private String timestamp;
    private Integer sequenceNumber;
}
