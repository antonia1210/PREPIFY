package com.anto.backend.repository;

import com.anto.backend.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {
    Optional<OtpToken> findTopByEmailOrderByExpiresAtDesc(String email);
}