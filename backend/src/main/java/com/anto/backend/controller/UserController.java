package com.anto.backend.controller;
import com.anto.backend.dto.LoginResponse;
import com.anto.backend.model.User;
import com.anto.backend.service.LogService;
import com.anto.backend.service.MaliciousDetectionService;
import com.anto.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final LogService logService;
    private final MaliciousDetectionService detectionService;
    public UserController(UserService userService, LogService logService, MaliciousDetectionService detectionService) {
        this.userService = userService;
        this.logService = logService;
        this.detectionService = detectionService;
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest request) {
        try {
            User registered = userService.register(user.getName(), user.getEmail(), user.getUsername(), user.getPassword(), user.getPreferences());
            logService.info("REGISTER", registered.getId(), "USER", "New user registered: " + registered.getEmail(), request.getRemoteAddr());
            return ResponseEntity.ok(registered);
        } catch (RuntimeException e) {
            logService.warn("REGISTER_FAILED", null, "ANONYMOUS", "Failed registration for: " + user.getEmail(), request.getRemoteAddr());
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletRequest request) {
        try {
            User loggedIn = userService.login(user.getEmail(), user.getPassword());
            String role = loggedIn.getRoles().stream().map(r -> r.getName()).collect(Collectors.joining(","));
            logService.info("LOGIN", loggedIn.getId(), role, "User logged in: " + loggedIn.getEmail(), request.getRemoteAddr());
            return ResponseEntity.ok(new LoginResponse(loggedIn));
        } catch (RuntimeException e) {
            logService.warn("LOGIN_FAILED", null, "ANONYMOUS", "Failed login for: " + user.getEmail(), request.getRemoteAddr());
            detectionService.analyze(null, "LOGIN_FAILED", request.getRemoteAddr());
            return ResponseEntity.status(401).body(e.getMessage());
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
}
