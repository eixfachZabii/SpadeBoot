// src/main/java/com/pokerapp/api/controller/HeatmapController.java
package com.pokerapp.api.controller;

import com.pokerapp.service.HeatmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/heatmap")
public class HeatmapController {

    @Autowired
    private HeatmapService heatmapService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHeatmapData() {
        return ResponseEntity.ok(heatmapService.getHeatmapData());
    }
}