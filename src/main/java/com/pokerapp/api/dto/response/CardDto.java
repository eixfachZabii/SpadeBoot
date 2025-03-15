// src/main/java/com/pokerapp/api/dto/response/CardDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

@Data
public class CardDto {
    private String suit;
    private String rank;
    private boolean hidden;
}
