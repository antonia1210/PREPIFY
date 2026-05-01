package com.anto.backend.repository;

import com.anto.backend.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryUserRepository {
    @jakarta.annotation.PostConstruct
    public void init() {
        save(new User(null, "Alice", "alice@gmail.com", "alice", "Alice1234!", "vegan"));
        save(new User(null, "Hannah", "hannah@gmail.com", "hannah", "Hannah12!", "vegetarian"));
    }
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public User save(User user) {
        if (user.getId() == null) user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }
    public List<User> findAll() {
        return List.copyOf(users.values());
    }
}