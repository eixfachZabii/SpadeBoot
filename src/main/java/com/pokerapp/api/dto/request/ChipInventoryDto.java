// src/main/java/com/pokerapp/api/dto/ChipInventoryDto.java
package com.pokerapp.api.dto.request;

import lombok.Data;

@Data
public class ChipInventoryDto {
    private int chip1;
    private int chip5;
    private int chip10;
    private int chip25;
    private int chip100;
    private int chip500;
    private int targetValue = 1000; // Default target value
}