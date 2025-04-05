// src/main/java/com/pokerapp/service/HeatmapService.java
package com.pokerapp.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HeatmapService {

    /**
     * Returns heatmap data for visualization
     * @return Map containing heatmap data
     */
    public Map<String, Object> getHeatmapData() {
        Map<String, Object> heatmapData = new HashMap<>();

        // This is a simplified version of the heatmap data from the Python code
        // You would likely want to implement more dynamic data generation based on your application's needs

        List<Map<String, Object>> data = new ArrayList<>();

        // Sample data - in a real implementation, this would come from your database or game state
        String[] playerNames = {
                "Bozzetti", "Huber", "Rogg", "Meierlohr", "Hoerter", "Simon", "Vorderbruegge", "Maier"
        };

        String[] handTypes = {
                "High Card", "Pair", "Two Pair", "Three of a Kind", "Straight",
                "Flush", "Full House", "Four of a Kind", "Straight Flush", "Royal Flush"
        };

        // Generate sample heatmap data
        for (String playerName : playerNames) {
            for (String handType : handTypes) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("player", playerName);
                entry.put("handType", handType);
                entry.put("value", Math.random() * 100); // Random value between 0-100
                data.add(entry);
            }
        }

        heatmapData.put("data", data);
        return heatmapData;
    }
}