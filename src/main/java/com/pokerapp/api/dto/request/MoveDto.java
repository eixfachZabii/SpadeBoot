// src/main/java/com/pokerapp/api/dto/request/MoveDto.java
package com.pokerapp.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class MoveDto {
    @NotNull
    private String type; // "CHECK", "CALL", "RAISE", "FOLD", "ALL_IN"

    private Double amount; // Required for RAISE

    public @NotNull String getType() {
        return type;
    }

    public void setType(@NotNull String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
