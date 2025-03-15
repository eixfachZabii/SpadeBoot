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

    // Standard getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getChips() {
        return chips;
    }

    public void setChips(Double chips) {
        this.chips = chips;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CardDto> getCards() {
        return cards;
    }

    public void setCards(List<CardDto> cards) {
        this.cards = cards;
    }

    public boolean isTurn() {
        return isTurn;
    }

    public void setTurn(boolean turn) {
        isTurn = turn;
    }
}