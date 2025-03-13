// src/main/java/com/pokerapp/api/dto/response/ReplayDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;
import java.util.List;

//@Data
public class ReplayDto {
    private Long id;
    private Long gameId;
    private Long tableId;
    private String startTime;
    private String endTime;
    private List<GameActionDto> actions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<GameActionDto> getActions() {
        return actions;
    }

    public void setActions(List<GameActionDto> actions) {
        this.actions = actions;
    }
}
