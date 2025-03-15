

// src/main/java/com/pokerapp/api/dto/response/GameStateDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class GameStateDto {
    private Long gameId;
    private String status;
    private Double pot;
    private List<CardDto> communityCards;
    private List<PlayerStateDto> players;
    private Long currentPlayerId;
    private Double currentBet;
    private String stage;
    private List<String> possibleActions;
    private List<MessageDto> messages;
}
