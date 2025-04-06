// src/main/java/com/pokerapp/api/dto/ChipDistributionResultDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class ChipDistributionDto {
    private boolean success = true;
    private String error;
    private int maxPlayers;
    private int valuePerPlayer;
    private int chipsPerPlayer;
    private int efficiency;
    private Map<String, Integer> playerDistribution;
}