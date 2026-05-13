package com.anto.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles = new ArrayList<>();

    public Permission() {}
    public Permission(String name) { this.name = name; }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}