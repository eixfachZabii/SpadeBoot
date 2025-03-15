// src/main/java/com/pokerapp/api/dto/response/PlayerStateDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.List;

//@Data
public class PlayerStateDto {
    private Long id;          // This should be the Player ID for consistency
    private Long userId;      // Add this field to explicitly include User ID
    private String username;
    private Double chips;
    private String status;
    private List<CardDto> cards;
    private boolean isTurn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
