// src/main/java/com/pokerapp/model/HeatmapDataPoint.java
package com.pokerapp.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDataPoint {
    private String x;
    private String y;
    private Double heat;
    private String symbol;
}