package com.anto.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "otp_tokens")
public class OtpToken {

    @Id
    private String id;
    private String email;
    private String code;
    private LocalDateTime expiresAt;
    private boolean used;

    public OtpToken() {}

    public OtpToken(String email, String code) {
        this.email = email;
        this.code = code;
        this.expiresAt = LocalDateTime.now().plusMinutes(10);
        this.used = false;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getCode() { return code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}