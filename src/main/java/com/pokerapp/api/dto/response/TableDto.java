// src/main/java/com/pokerapp/api/dto/response/TableDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

//@Data
public class TableDto {
    private Long id;
    private String name;
    private String description;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private Double minBuyIn;
    private Double maxBuyIn;
    private Boolean isPrivate;
    private Long ownerId;
    private Boolean hasActiveGame;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(Integer currentPlayers) {
        this.currentPlayers = currentPlayers;
    }

    public Double getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(Double minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    public Double getMaxBuyIn() {
        return maxBuyIn;
    }

    public void setMaxBuyIn(Double maxBuyIn) {
        this.maxBuyIn = maxBuyIn;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getHasActiveGame() {
        return hasActiveGame;
    }

    public void setHasActiveGame(Boolean hasActiveGame) {
        this.hasActiveGame = hasActiveGame;
    }
}
