package com.anto.backend.service;

import com.anto.backend.model.Message;
import com.anto.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> getConversationsByUser(Integer userId) {
        return messageRepository.findByUserId(userId);
    }
    public List<Message> getConversation(Integer userId1, Integer userId2) {
        String conversationId = Math.min(userId1, userId2) + "_" + Math.max(userId1, userId2);
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }
}