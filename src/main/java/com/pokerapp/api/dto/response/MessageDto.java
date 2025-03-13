// src/main/java/com/pokerapp/api/dto/response/MessageDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

//@Data
public class MessageDto {
    private String type; // INFO, ERROR, NOTIFICATION
    private String content;
    private Long playerId;
    private String timestamp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
