package com.anto.backend.service;

import com.anto.backend.model.Log;
import com.anto.backend.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiMonitoringService {

    private final LogRepository logRepository;
    private final WebClient webClient;

    public AiMonitoringService(LogRepository logRepository) {
        this.logRepository = logRepository;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public Map<String, Object> analyzeRecentBehavior() {
        // Get logs from last 10 minutes
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        List<Log> recentLogs = logRepository.findAll().stream()
                .filter(log -> log.getTimestamp() != null &&
                        log.getTimestamp().isAfter(since))
                .collect(Collectors.toList());

        if (recentLogs.isEmpty()) {
            return Map.of(
                    "message", "No recent activity to analyze",
                    "suspicious", false,
                    "analysis", "No logs found in the last 10 minutes"
            );
        }

        // Group by IP
        Map<String, List<Log>> byIp = recentLogs.stream()
                .collect(Collectors.groupingBy(log ->
                        log.getIpAddress() != null ? log.getIpAddress() : "unknown"));

        // Build summary for LLM
        StringBuilder summary = new StringBuilder();
        summary.append("Analyze this server activity log for suspicious behavior:\n\n");

        for (Map.Entry<String, List<Log>> entry : byIp.entrySet()) {
            String ip = entry.getKey();
            List<Log> logs = entry.getValue();

            Map<String, Long> actionCounts = logs.stream()
                    .collect(Collectors.groupingBy(
                            log -> log.getAction() != null ? log.getAction() : "UNKNOWN",
                            Collectors.counting()));

            summary.append(String.format("IP: %s\n", ip));
            summary.append(String.format("  Total requests: %d\n", logs.size()));
            summary.append(String.format("  Actions: %s\n", actionCounts));

            long failedLogins = logs.stream()
                    .filter(l -> "LOGIN_FAILED".equals(l.getAction()))
                    .count();
            if (failedLogins > 0) {
                summary.append(String.format("  Failed logins: %d\n", failedLogins));
            }
            summary.append("\n");
        }

        summary.append("\nBased on this data, identify:\n");
        summary.append("1. Which IPs show suspicious behavior and why\n");
        summary.append("2. What type of attack this could be\n");
        summary.append("3. Recommended action\n");
        summary.append("Keep response concise, max 200 words.");

        // Call Ollama
        String aiResponse = callOllama(summary.toString());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("logsAnalyzed", recentLogs.size());
        result.put("uniqueIps", byIp.size());
        result.put("timeWindow", "Last 10 minutes");
        result.put("suspicious", aiResponse.toLowerCase().contains("suspicious") ||
                aiResponse.toLowerCase().contains("attack") ||
                aiResponse.toLowerCase().contains("brute"));
        result.put("analysis", aiResponse);
        result.put("logSummary", byIp.entrySet().stream()
                .map(e -> Map.of(
                        "ip", e.getKey(),
                        "requestCount", e.getValue().size()
                )).toList());
        return result;
    }

    private String callOllama(String prompt) {
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", "llama3.2");
            request.put("prompt", prompt);
            request.put("stream", false);

            Map response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null ?
                    (String) response.get("response") :
                    "AI analysis unavailable";
        } catch (Exception e) {
            return "AI analysis error: " + e.getMessage();
        }
    }
}