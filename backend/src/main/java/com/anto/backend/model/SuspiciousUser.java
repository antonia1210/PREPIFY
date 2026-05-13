package com.anto.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "suspicious_users")
public class SuspiciousUser {
    @Id
    private String id;
    private Integer userId;
    private String username;
    private String email;
    private String reason;
    private int violationCount;
    private LocalDateTime firstDetected;
    private LocalDateTime lastDetected;
    private List<String> actions;

    public SuspiciousUser() {}

    public SuspiciousUser(Integer userId, String username, String email, String reason, List<String> actions) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.reason = reason;
        this.violationCount = 1;
        this.firstDetected = LocalDateTime.now();
        this.lastDetected = LocalDateTime.now();
        this.actions = actions;
    }

    public String getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getReason() { return reason; }
    public int getViolationCount() { return violationCount; }
    public LocalDateTime getFirstDetected() { return firstDetected; }
    public LocalDateTime getLastDetected() { return lastDetected; }
    public List<String> getActions() { return actions; }
    public void setId(String id) { this.id = id; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setReason(String reason) { this.reason = reason; }
    public void setViolationCount(int violationCount) { this.violationCount = violationCount; }
    public void setFirstDetected(LocalDateTime firstDetected) { this.firstDetected = firstDetected; }
    public void setLastDetected(LocalDateTime lastDetected) { this.lastDetected = lastDetected; }
    public void setActions(List<String> actions) { this.actions = actions; }
}