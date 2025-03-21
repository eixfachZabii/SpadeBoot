package com.pokerapp.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableSettingsDto {
    private String name;
    private String description;
    private Integer maxPlayers;
    private Integer minBuyIn;
    private Integer maxBuyIn;
    private Boolean isPrivate;
}