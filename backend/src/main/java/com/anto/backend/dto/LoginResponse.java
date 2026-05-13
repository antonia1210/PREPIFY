package com.anto.backend.dto;

import com.anto.backend.model.Role;
import com.anto.backend.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class LoginResponse {
    private Integer id;
    private String name;
    private String email;
    private String username;
    private String preferences;
    private List<String> roles;
    private List<String> permissions;

    public LoginResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.preferences = user.getPreferences();
        this.roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        this.permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct().collect(Collectors.toList());
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPreferences() { return preferences; }
    public List<String> getRoles() { return roles; }
    public List<String> getPermissions() { return permissions; }
}