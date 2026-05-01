package com.anto.backend.controller;

import com.anto.backend.dto.GeneratorStartRequest;
import com.anto.backend.service.RecipeGeneratorService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recipes/generator")
public class RecipeGeneratorController {

    private final RecipeGeneratorService generatorService;

    public RecipeGeneratorController(RecipeGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody(required = false) GeneratorStartRequest request) {
        int batchSize = request != null && request.getBatchSize() != null ? request.getBatchSize() : 3;
        int intervalMillis = request != null && request.getInterval() != null ? request.getInterval() : 5000;

        String message = generatorService.start(batchSize, intervalMillis);

        return Map.of(
                "message", message,
                "running", generatorService.isRunning(),
                "batchSize", batchSize,
                "intervalMillis", intervalMillis
        );
    }

    @PostMapping("/stop")
    public Map<String, Object> stop() {
        String message = generatorService.stop();

        return Map.of(
                "message", message,
                "running", generatorService.isRunning()
        );
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "running", generatorService.isRunning()
        );
    }
}