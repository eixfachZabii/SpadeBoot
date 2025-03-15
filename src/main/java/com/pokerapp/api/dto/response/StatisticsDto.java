// src/main/java/com/pokerapp/api/dto/response/StatisticsDto.java
package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsDto {
    private Long userId;
    private Long playerId;
    private String username;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Double winRate;
    private Double totalWinnings;
}
