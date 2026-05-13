package com.anto.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String content;
    private LocalDateTime timestamp;
    private String conversationId;

    public Message() {}

    public Message(Integer senderId, String senderName, Integer receiverId, String receiverName, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.conversationId = generateConversationId(senderId, receiverId);
    }

    private String generateConversationId(Integer id1, Integer id2) {
        return Math.min(id1, id2) + "_" + Math.max(id1, id2);
    }

    public String getId() { return id; }
    public Integer getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public Integer getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getConversationId() { return conversationId; }
    public void setId(String id) { this.id = id; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}