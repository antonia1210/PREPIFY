package com.anto.backend.service;

import com.anto.backend.model.OtpToken;
import com.anto.backend.repository.OtpTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    public void generateAndSend(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        otpTokenRepository.save(new OtpToken(email, code));
        emailService.sendOtp(email, code);
    }

    public boolean verify(String email, String code) {
        OtpToken otp = otpTokenRepository
                .findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new RuntimeException("No OTP found"));

        if (otp.isUsed()) throw new RuntimeException("OTP already used");
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("OTP expired");
        if (!otp.getCode().equals(code)) throw new RuntimeException("Invalid OTP code");

        otp.setUsed(true);
        otpTokenRepository.save(otp);
        return true;
    }
}