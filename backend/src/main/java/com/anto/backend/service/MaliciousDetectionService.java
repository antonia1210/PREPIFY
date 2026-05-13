package com.anto.backend.service;

import com.anto.backend.model.Log;
import com.anto.backend.model.SuspiciousUser;
import com.anto.backend.model.User;
import com.anto.backend.repository.LogRepository;
import com.anto.backend.repository.SuspiciousUserRepository;
import com.anto.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaliciousDetectionService {

    private final LogRepository logRepository;
    private final SuspiciousUserRepository suspiciousUserRepository;
    private final UserRepository userRepository;

    private static final int MAX_FAILED_LOGINS = 3;
    private static final int MAX_DELETES_PER_MINUTE = 3;
    private static final int MAX_REQUESTS_PER_MINUTE = 20;

    public MaliciousDetectionService(LogRepository logRepository, SuspiciousUserRepository suspiciousUserRepository, UserRepository userRepository) {
        this.logRepository = logRepository;
        this.suspiciousUserRepository = suspiciousUserRepository;
        this.userRepository = userRepository;
    }

    public void analyze(Integer userId, String action, String ip) {
        checkFailedLogins(ip);
        if (userId != null) {
            checkExcessiveDeletes(userId);
            checkExcessiveRequests(userId);
        }
    }

    private void checkFailedLogins(String ip) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Log> failedLogins = logRepository.findByAction("LOGIN_FAILED").stream()
                .filter(l -> l.getTimestamp().isAfter(oneHourAgo))
                .filter(l -> ip.equals(l.getIpAddress()))
                .collect(Collectors.toList());

        if (failedLogins.size() >= MAX_FAILED_LOGINS) {
            flagSuspicious(null, "unknown", ip,
                    "Too many failed login attempts (" + failedLogins.size() + ") from IP: " + ip,
                    failedLogins.stream().map(Log::getAction).collect(Collectors.toList()));
        }
    }

    private void checkExcessiveDeletes(Integer userId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        List<Log> deletes = logRepository.findByUserId(userId).stream()
                .filter(l -> l.getAction().equals("DELETE_RECIPE"))
                .filter(l -> l.getTimestamp().isAfter(oneMinuteAgo))
                .collect(Collectors.toList());

        if (deletes.size() >= MAX_DELETES_PER_MINUTE) {
            userRepository.findById(userId).ifPresent(user -> {
                flagSuspicious(userId, user.getUsername(), user.getEmail(),
                        "Excessive deletes (" + deletes.size() + ") in 1 minute",
                        deletes.stream().map(Log::getAction).collect(Collectors.toList()));
            });
        }
    }

    private void checkExcessiveRequests(Integer userId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        List<Log> requests = logRepository.findByUserId(userId).stream()
                .filter(l -> l.getTimestamp().isAfter(oneMinuteAgo))
                .collect(Collectors.toList());

        if (requests.size() >= MAX_REQUESTS_PER_MINUTE) {
            userRepository.findById(userId).ifPresent(user -> {
                flagSuspicious(userId, user.getUsername(), user.getEmail(),
                        "Excessive requests (" + requests.size() + ") in 1 minute",
                        requests.stream().map(Log::getAction).collect(Collectors.toList()));
            });
        }
    }

    private void flagSuspicious(Integer userId, String username, String email, String reason, List<String> actions) {
        if (userId != null && suspiciousUserRepository.existsByUserId(userId)) {
            suspiciousUserRepository.findByUserId(userId).ifPresent(existing -> {
                existing.setViolationCount(existing.getViolationCount() + 1);
                existing.setLastDetected(LocalDateTime.now());
                existing.setReason(reason);
                suspiciousUserRepository.save(existing);
            });
        } else {
            suspiciousUserRepository.save(new SuspiciousUser(userId, username, email, reason, actions));
        }
    }

    public List<SuspiciousUser> getSuspiciousUsers() {
        return suspiciousUserRepository.findAll();
    }
}