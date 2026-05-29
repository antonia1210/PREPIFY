package com.anto.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMagicLink(String toEmail, String token) {
        String link = frontendBaseUrl + "/auth/magic?token=" + token;
        String subject = "Your Prepify Magic Link";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2 style="color: #C08497;">PREPIFY</h2>
                    <p>Click the button below to log in. This link expires in <strong>15 minutes</strong>.</p>
                    <a href="%s"
                       style="display:inline-block; padding: 12px 24px; background:#C08497;
                              color:white; text-decoration:none; border-radius:6px; font-weight:bold;">
                        Log in to Prepify
                    </a>
                    <p style="color:#999; font-size:0.85rem; margin-top:1rem;">
                        If you didn't request this, you can safely ignore this email.
                    </p>
                </div>
                """.formatted(link);
        sendHtml(toEmail, subject, body);
    }

    public void sendPasswordReset(String toEmail, String token) {
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        String subject = "Reset your Prepify password";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2 style="color: #C08497;">PREPIFY</h2>
                    <p>Click the button below to reset your password. This link expires in <strong>30 minutes</strong>.</p>
                    <a href="%s"
                       style="display:inline-block; padding: 12px 24px; background:#C08497;
                              color:white; text-decoration:none; border-radius:6px; font-weight:bold;">
                        Reset Password
                    </a>
                    <p style="color:#999; font-size:0.85rem; margin-top:1rem;">
                        If you didn't request this, you can safely ignore this email.
                    </p>
                </div>
                """.formatted(link);
        sendHtml(toEmail, subject, body);
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Prepify");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    public void sendOtp(String toEmail, String code) {
        String subject = "Your Prepify login code";
        String body = """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                <h2 style="color: #C08497;">PREPIFY</h2>
                <p>Your one-time login code is:</p>
                <div style="font-size: 42px; font-weight: bold; letter-spacing: 10px;
                            color: #562121; text-align: center; margin: 20px 0;">
                    %s
                </div>
                <p style="color:#999; font-size:0.85rem;">
                    This code expires in <strong>10 minutes</strong>.
                    If you didn't try to log in, ignore this email.
                </p>
            </div>
            """.formatted(code);
        sendHtml(toEmail, subject, body);
    }
}