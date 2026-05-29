package com.anto.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "sessions")
public class Session {
    @Id
    private String id;
    private Integer userId;
    private String username;
    private String token;
    private String refreshToken;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private String ipAddress;

    public Session() {}

    public Session(Integer userId, String username, String token, String ipAddress) {
        this.userId = userId;
        this.username = username;
        this.token = token;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        this.active = true;
        this.ipAddress = ipAddress;
    }

    public String getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public String getIpAddress() { return ipAddress; }
    public void setId(String id) { this.id = id; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setToken(String token) { this.token = token; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setActive(boolean active) { this.active = active; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}