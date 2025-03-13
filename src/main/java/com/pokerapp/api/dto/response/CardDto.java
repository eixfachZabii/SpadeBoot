// src/main/java/com/pokerapp/api/dto/response/CardDto.java
package com.pokerapp.api.dto.response;

import lombok.Data;

//@Data
public class CardDto {
    private String suit;
    private String rank;
    private boolean hidden;

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
