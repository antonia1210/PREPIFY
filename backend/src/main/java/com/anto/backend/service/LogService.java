package com.anto.backend.service;

import com.anto.backend.model.Log;
import com.anto.backend.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {
    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }
    public void info(String action, Integer userId, String groupId, String details, String ip) {
        logRepository.save(new Log("INFO", action, userId, groupId, details, ip));
    }

    public void warn(String action, Integer userId, String groupId, String details, String ip) {
        logRepository.save(new Log("WARN", action, userId, groupId, details, ip));
    }

    public void error(String action, Integer userId, String groupId, String details, String ip) {
        logRepository.save(new Log("ERROR", action, userId, groupId, details, ip));
    }
    public List<Log> getAll() { return logRepository.findAll(); }
    public List<Log> getByUser(Integer userId) { return logRepository.findByUserId(userId); }
    public List<Log> getByLevel(String level) { return logRepository.findByLevel(level); }
}