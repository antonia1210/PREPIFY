package com.anto.backend.model;

public class User {
    private Integer id;
    private String name;
    private String email;
    private String username;
    private String password;
    private String preferences;

    public User() {}
    public User(Integer id, String name, String email, String username, String password, String preferences) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.preferences = preferences;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPreferences() { return preferences; }
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}