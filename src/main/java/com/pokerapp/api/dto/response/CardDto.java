// src/main/java/com/pokerapp/api/dto/response/CardDto.java
package com.pokerapp.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardDto {
    private String suit;
    private String rank;
    private boolean hidden;

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
