package com.anto.backend;

import com.anto.backend.service.*;
import com.anto.backend.model.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ServiceTests {

    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MaliciousDetectionService detectionService;

    @Test
    void testLogInfo() {
        logService.info("TEST_ACTION", 1, "USER", "test details", "127.0.0.1");
        List<Log> logs = logService.getByUser(1);
        assertFalse(logs.isEmpty());
    }

    @Test
    void testLogWarn() {
        logService.warn("TEST_WARN", null, "ANONYMOUS", "warn details", "127.0.0.1");
        List<Log> logs = logService.getByLevel("WARN");
        assertFalse(logs.isEmpty());
    }

    @Test
    void testLogError() {
        logService.error("TEST_ERROR", null, "ANONYMOUS", "error details", "127.0.0.1");
        List<Log> logs = logService.getByLevel("ERROR");
        assertFalse(logs.isEmpty());
    }

    @Test
    void testGetAllLogs() {
        logService.info("TEST", 1, "USER", "details", "127.0.0.1");
        assertFalse(logService.getAll().isEmpty());
    }

    @Test
    void testGetConversation() {
        List<?> messages = messageService.getConversation(1, 2);
        assertNotNull(messages);
    }

    @Test
    void testAnalyzeNullUser() {
        assertDoesNotThrow(() -> detectionService.analyze(null, "LOGIN_FAILED", "127.0.0.1"));
    }

    @Test
    void testGetSuspiciousUsers() {
        assertNotNull(detectionService.getSuspiciousUsers());
    }

    @Test
    void testSaveMessage() {
        com.anto.backend.model.Message message = new com.anto.backend.model.Message();
        message.setSenderId(1);
        message.setSenderName("Alice");
        message.setReceiverId(2);
        message.setReceiverName("Bob");
        message.setContent("Hello!");
        message.setConversationId("1_2");
        message.setTimestamp(java.time.LocalDateTime.now());
        com.anto.backend.model.Message saved = messageService.save(message);
        assertNotNull(saved.getId());
        assertEquals("Hello!", saved.getContent());
    }

    @Test
    void testGetConversationWithMessages() {
        com.anto.backend.model.Message message = new com.anto.backend.model.Message();
        message.setSenderId(1);
        message.setSenderName("Alice");
        message.setReceiverId(2);
        message.setReceiverName("Bob");
        message.setContent("Test message");
        message.setConversationId("1_2");
        message.setTimestamp(java.time.LocalDateTime.now());
        messageService.save(message);
        List<?> messages = messageService.getConversation(1, 2);
        assertFalse(messages.isEmpty());
    }

    @Test
    void testGetConversationsByUser() {
        com.anto.backend.model.Message message = new com.anto.backend.model.Message();
        message.setSenderId(1);
        message.setSenderName("Alice");
        message.setReceiverId(3);
        message.setReceiverName("Bob");
        message.setContent("Hello Bob");
        message.setConversationId("1_3");
        message.setTimestamp(java.time.LocalDateTime.now());
        messageService.save(message);
        assertFalse(messageService.getConversationsByUser(1).isEmpty());
    }

    @Test
    void testAnalyzeWithUserId() {
        assertDoesNotThrow(() -> detectionService.analyze(1, "DELETE_RECIPE", "127.0.0.1"));
    }

    @Test
    void testAnalyzeFailedLoginMultipleTimes() {
        for (int i = 0; i < 4; i++) {
            detectionService.analyze(null, "LOGIN_FAILED", "10.0.0.1");
        }
        assertNotNull(detectionService.getSuspiciousUsers());
    }

    @Autowired
    private RecipeGeneratorService recipeGeneratorService;

    @Test
    void testGeneratorStart() {
        String result = recipeGeneratorService.start(1, 10000);
        assertNotNull(result);
        assertTrue(recipeGeneratorService.isRunning());
        recipeGeneratorService.stop();
    }

    @Test
    void testGeneratorStop() {
        recipeGeneratorService.start(1, 10000);
        String result = recipeGeneratorService.stop();
        assertNotNull(result);
        assertFalse(recipeGeneratorService.isRunning());
    }

    @Test
    void testGeneratorStartAlreadyRunning() {
        recipeGeneratorService.start(1, 10000);
        String result = recipeGeneratorService.start(1, 10000);
        assertEquals("Generator is already running", result);
        recipeGeneratorService.stop();
    }

    @Test
    void testGeneratorStopNotRunning() {
        String result = recipeGeneratorService.stop();
        assertEquals("Generator is not running", result);
    }

    @Test
    void testIsRunning() {
        assertFalse(recipeGeneratorService.isRunning());
    }
}