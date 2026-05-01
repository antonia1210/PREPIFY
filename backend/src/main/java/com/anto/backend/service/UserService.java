package com.anto.backend.service;

import com.anto.backend.model.User;
import com.anto.backend.repository.InMemoryUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final InMemoryUserRepository userRepository;
    public UserService(InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User register(String name, String email, String username, String password, String preferences) {
        if(userRepository.existsByEmail(username)) throw new RuntimeException("User already registered");
        return userRepository.save(new User(null, name, email,username, password, preferences));
    }
    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }
    public User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
