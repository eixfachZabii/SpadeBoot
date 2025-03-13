

// src/main/java/com/pokerapp/api/dto/response/GameStateDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.List;

//@Data
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

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPot() {
        return pot;
    }

    public void setPot(Double pot) {
        this.pot = pot;
    }

    public List<CardDto> getCommunityCards() {
        return communityCards;
    }

    public void setCommunityCards(List<CardDto> communityCards) {
        this.communityCards = communityCards;
    }

    public List<PlayerStateDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerStateDto> players) {
        this.players = players;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public Double getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(Double currentBet) {
        this.currentBet = currentBet;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<String> getPossibleActions() {
        return possibleActions;
    }

    public void setPossibleActions(List<String> possibleActions) {
        this.possibleActions = possibleActions;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}
