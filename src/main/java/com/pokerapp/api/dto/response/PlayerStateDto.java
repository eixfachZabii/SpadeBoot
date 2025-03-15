// src/main/java/com/pokerapp/api/dto/response/PlayerStateDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class PlayerStateDto {
    private Long id;          // Player ID
    private Long userId;      // User ID
    private String username;
    private Double chips;
    private String status;
    private List<CardDto> cards;
    private boolean isTurn;   // Flag indicating if it's this player's turn
}