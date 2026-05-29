package com.anto.backend.controller;

import com.anto.backend.service.AiMonitoringService;
import com.anto.backend.service.SeederService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SeederService seederService;
    private final AiMonitoringService aiMonitoringService;

    public AdminController(SeederService seederService, AiMonitoringService aiMonitoringService) {
        this.seederService = seederService;
        this.aiMonitoringService = aiMonitoringService;
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seed(@RequestBody Map<String, Integer> body) {
        int users = body.getOrDefault("users", 500);
        int recipes = body.getOrDefault("recipes", 2000);
        Map<String, Integer> result = seederService.seed(users, recipes);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ai-monitor")
    public ResponseEntity<?> aiMonitor() {
        return ResponseEntity.ok(aiMonitoringService.analyzeRecentBehavior());
    }

}