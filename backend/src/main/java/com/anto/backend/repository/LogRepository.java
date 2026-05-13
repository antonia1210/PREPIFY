package com.anto.backend.repository;

import com.anto.backend.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<Log, String> {
    List<Log> findByUserId(Integer userId);
    List<Log> findByLevel(String level);
    List<Log> findByAction(String action);
    List<Log> findByIpAddress(String ipAddress);
}