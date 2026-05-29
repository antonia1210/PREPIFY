package com.anto.backend.repository;

import com.anto.backend.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    Optional<Session> findByToken(String token);
    Optional<Session> findByRefreshToken(String refreshToken);
    List<Session> findByUserId(Integer userId);
    List<Session> findByActive(boolean active);
}