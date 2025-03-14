// src/main/java/com/pokerapp/api/dto/response/StatisticsDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

//@Data
public class StatisticsDto {
    private Long userId;
    private Long playerId;
    private String username;
    private Integer gamesPlayed;
    private Integer gamesWon;
    private Double winRate;
    private Double totalWinnings;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Integer getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(Integer gamesWon) {
        this.gamesWon = gamesWon;
    }

    public Double getWinRate() {
        return winRate;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public Double getTotalWinnings() {
        return totalWinnings;
    }

    public void setTotalWinnings(Double totalWinnings) {
        this.totalWinnings = totalWinnings;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}
