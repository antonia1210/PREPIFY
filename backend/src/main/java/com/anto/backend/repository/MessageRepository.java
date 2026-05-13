package com.anto.backend.repository;

import com.anto.backend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    @Query("{ '$or': [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }")
    List<Message> findByUserId(Integer userId);

    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);
}
