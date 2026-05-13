package com.anto.backend.controller;

import com.anto.backend.model.Message;
import com.anto.backend.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/send")
    public Message sendMessage(@RequestBody Message message) {
        String conversationId = Math.min(message.getSenderId(), message.getReceiverId()) + "_" + Math.max(message.getSenderId(), message.getReceiverId());
        message.setConversationId(conversationId);
        message.setTimestamp(java.time.LocalDateTime.now());
        Message saved = messageService.save(message);
        messagingTemplate.convertAndSend("/topic/chat/" + saved.getConversationId(), saved);
        return saved;
    }
    @GetMapping("/conversations/{userId}")
    public List<Map<String, Object>> getConversations(@PathVariable Integer userId) {
        List<Message> messages = messageService.getConversationsByUser(userId);
        return messages.stream()
                .filter(m -> m.getConversationId() != null)
                .collect(Collectors.groupingBy(Message::getConversationId))
                .entrySet().stream()
                .map(e -> {
                    Message last = e.getValue().get(e.getValue().size() - 1);
                    Map<String, Object> conv = new java.util.HashMap<>();
                    conv.put("conversationId", e.getKey());
                    conv.put("lastMessage", last.getContent());
                    conv.put("timestamp", last.getTimestamp());
                    conv.put("otherUserId", last.getSenderId().equals(userId) ? last.getReceiverId() : last.getSenderId());
                    conv.put("otherUserName", last.getSenderId().equals(userId) ? last.getReceiverName() : last.getSenderName());
                    return conv;
                })
                .collect(Collectors.toList());
    }
    @GetMapping("/conversation")
    public List<Message> getConversation(
            @RequestParam Integer userId1,
            @RequestParam Integer userId2) {
        return messageService.getConversation(userId1, userId2);
    }
}