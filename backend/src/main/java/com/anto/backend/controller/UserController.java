package com.anto.backend.controller;
import com.anto.backend.dto.LoginResponse;
import com.anto.backend.model.User;
import com.anto.backend.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final LogService logService;
    private final MaliciousDetectionService detectionService;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final OtpService otpService;

    public UserController(UserService userService, LogService logService, MaliciousDetectionService detectionService, JwtService jwtService, SessionService sessionService, EmailService emailService, OtpService otpService) {
        this.userService = userService;
        this.logService = logService;
        this.detectionService = detectionService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.emailService = emailService;
        this.otpService = otpService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest request) {
        try {
            User registered = userService.register(
                    user.getName(), user.getEmail(), user.getUsername(),
                    user.getPassword(), user.getPreferences(),
                    user.getSecurityQuestion(), user.getSecurityAnswer()
            );
            logService.info("REGISTER", registered.getId(), "USER",
                    "New user registered: " + registered.getEmail(),
                    request.getRemoteAddr());
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        try {
            User user = userService.login(body.get("email"), body.get("password"));

            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getName()).toList();
            List<String> permissions = user.getRoles().stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(p -> p.getName()).toList();

            // Skip OTP for admin users
            if (roles.contains("ADMIN")) {
                String jwt = jwtService.generateToken(user.getId(), user.getEmail(),
                        roles.get(0), permissions);
                String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
                sessionService.createSession(user.getId(), user.getUsername(),
                        jwt, refreshToken, request.getRemoteAddr());
                logService.info("LOGIN_SUCCESS", user.getId(), null,
                        "Admin login: " + user.getEmail(), request.getRemoteAddr());
                return ResponseEntity.ok(Map.of(
                        "token", jwt,
                        "refreshToken", refreshToken,
                        "user", Map.of(
                                "id", user.getId(),
                                "name", user.getName(),
                                "email", user.getEmail(),
                                "username", user.getUsername(),
                                "roles", roles,
                                "permissions", permissions
                        )
                ));
            }

            // Regular users go through OTP
            otpService.generateAndSend(user.getEmail());
            logService.info("LOGIN_ATTEMPT", user.getId(), null,
                    "Password verified for: " + user.getEmail(), request.getRemoteAddr());
            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent to your email",
                    "email", user.getEmail()
            ));

        } catch (RuntimeException e) {
            detectionService.analyze(null, "LOGIN_FAILED", request.getRemoteAddr());
            logService.warn("LOGIN_FAILED", null, null,
                    e.getMessage(), request.getRemoteAddr());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        try {
            String email = body.get("email");
            String code = body.get("code");
            otpService.verify(email, code);
            logService.info("OTP_VERIFIED", null, null,
                    "OTP verified for: " + email, request.getRemoteAddr());
            return ResponseEntity.ok(Map.of("verified", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/verify-security")
    public ResponseEntity<?> verifySecurity(@RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        try {
            String email = body.get("email");
            String answer = body.get("answer");

            boolean correct = userService.verifySecurityAnswer(email, answer);
            if (!correct) {
                return ResponseEntity.status(401).body(Map.of("error", "Incorrect answer"));
            }

            User user = userService.findByEmail(email);
            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getName()).toList();
            List<String> permissions = user.getRoles().stream()
                    .flatMap(r -> r.getPermissions().stream())
                    .map(p -> p.getName()).toList();

            String jwt = jwtService.generateToken(user.getId(), user.getEmail(),
                    roles.isEmpty() ? "USER" : roles.get(0), permissions);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
            sessionService.createSession(user.getId(), user.getUsername(),
                    jwt, refreshToken, request.getRemoteAddr());

            logService.info("LOGIN_SUCCESS", user.getId(), null,
                    "3FA complete for: " + email, request.getRemoteAddr());

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "refreshToken", refreshToken,
                    "user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "username", user.getUsername(),
                            "roles", roles,
                            "permissions", permissions
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id){
        return userService.getUserById(id);
    }
    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
        return sessionService.findByRefreshToken(refreshToken).map(session -> {
            if (!session.isActive()) return ResponseEntity.status(401).body("Session expired");
            Integer userId = jwtService.extractUserId(refreshToken);
            try {
                User user = userService.getUserById(userId);
                String role = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.joining(","));
                List<String> permissions = user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getName()).distinct().collect(Collectors.toList());
                String newToken = jwtService.generateToken(userId, user.getEmail(), role, permissions);
                session.setToken(newToken);
                sessionService.save(session);
                Map<String, Object> response = new HashMap<>();
                response.put("token", newToken);
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                return ResponseEntity.status(401).body("User not found");
            }
        }).orElse(ResponseEntity.status(401).body("Session not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            sessionService.invalidateSession(token);
        }
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            userService.forgotPassword(body.get("email"));
            return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            userService.resetPassword(body.get("resetToken"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@RequestParam String email) {
        try {
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(Map.of(
                    "question", user.getSecurityQuestion() != null ?
                            user.getSecurityQuestion() : ""
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @PostMapping("/security-question/setup")
    public ResponseEntity<?> setupSecurityQuestion(@RequestBody Map<String, String> body) {
        try {
            userService.setupSecurityQuestion(
                    body.get("email"),
                    body.get("question"),
                    body.get("answer")
            );
            return ResponseEntity.ok(Map.of("message", "Security question set"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
