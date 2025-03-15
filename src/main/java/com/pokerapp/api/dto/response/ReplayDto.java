// src/main/java/com/pokerapp/api/dto/response/ReplayDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ReplayDto {
    private Long id;
    private Long gameId;
    private Long tableId;
    private String startTime;
    private String endTime;
    private List<GameActionDto> actions;
}
