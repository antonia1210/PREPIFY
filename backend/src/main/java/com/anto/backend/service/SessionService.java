package com.anto.backend.service;

import com.anto.backend.model.Session;
import com.anto.backend.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(Integer userId, String username, String token, String refreshToken, String ip) {
        Session session = new Session(userId, username, token, ip);
        session.setRefreshToken(refreshToken);
        return sessionRepository.save(session);
    }

    public void invalidateSession(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }

    public boolean isSessionActive(String token) {
        return sessionRepository.findByToken(token)
                .map(Session::isActive)
                .orElse(false);
    }

    public Optional<Session> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }

    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    public List<Session> getActiveSessions() {
        return sessionRepository.findByActive(true);
    }
}