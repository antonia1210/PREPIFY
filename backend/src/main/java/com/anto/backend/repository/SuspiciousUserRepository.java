package com.anto.backend.repository;

import com.anto.backend.model.SuspiciousUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuspiciousUserRepository extends MongoRepository<SuspiciousUser, String> {
    Optional<SuspiciousUser> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
}