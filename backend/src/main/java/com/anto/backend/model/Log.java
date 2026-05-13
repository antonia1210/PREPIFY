package com.anto.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "logs")
public class Log {
    @Id
    private String id;
    private LocalDateTime timestamp;
    private String level;
    private String action;
    private Integer userId;
    private String groupId;
    private String details;
    private String ipAddress;

    public Log() {}

    public Log(String level, String action, Integer userId, String groupId, String details, String ipAddress) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.action = action;
        this.userId = userId;
        this.groupId = groupId;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getAction() { return action; }
    public Integer getUserId() { return userId; }
    public String getGroupId() { return groupId; }
    public String getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public void setId(String id) { this.id = id; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setLevel(String level) { this.level = level; }
    public void setAction(String action) { this.action = action; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setDetails(String details) { this.details = details; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}