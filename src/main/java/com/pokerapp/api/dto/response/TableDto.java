// src/main/java/com/pokerapp/api/dto/response/TableDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

@Data
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
}
