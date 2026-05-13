package com.anto.backend.controller;

import com.anto.backend.model.Log;
import com.anto.backend.service.LogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public List<Log> getAll() {
        return logService.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<Log> getByUser(@PathVariable Integer userId) {
        return logService.getByUser(userId);
    }

    @GetMapping("/level/{level}")
    public List<Log> getByLevel(@PathVariable String level) {
        return logService.getByLevel(level);
    }
}