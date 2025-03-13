// src/main/java/com/pokerapp/api/dto/request/ChatMessageDto.java
package com.pokerapp.api.dto.request;

import lombok.Data;

//@Data
public class ChatMessageDto {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
