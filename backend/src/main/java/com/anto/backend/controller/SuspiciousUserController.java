package com.anto.backend.controller;

import com.anto.backend.model.SuspiciousUser;
import com.anto.backend.service.MaliciousDetectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suspicious")
public class SuspiciousUserController {

    private final MaliciousDetectionService detectionService;

    public SuspiciousUserController(MaliciousDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping
    public List<SuspiciousUser> getSuspiciousUsers() {
        return detectionService.getSuspiciousUsers();
    }
}