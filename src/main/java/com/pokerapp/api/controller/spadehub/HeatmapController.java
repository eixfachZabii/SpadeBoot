// src/main/java/com/pokerapp/api/controller/HeatmapController.java
package com.pokerapp.api.controller.spadehub;

import com.pokerapp.service.spadehub.HeatmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/heatmap")
public class HeatmapController {

    @Autowired
    private HeatmapService heatmapService;

    @GetMapping
    public ResponseEntity<List<HeatmapService.HeatmapDataPoint>> getHeatmapData() {
        return ResponseEntity.ok(heatmapService.getHeatmapData());
    }
}