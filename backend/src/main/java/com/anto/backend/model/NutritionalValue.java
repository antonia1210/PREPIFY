package com.anto.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "recipe_nutritional_values")
public class NutritionalValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private double amount;
    private String unit;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public NutritionalValue() {}

    public NutritionalValue(String name, double amount, String unit, Recipe recipe) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.recipe = recipe;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getUnit() { return unit; }
    public Recipe getRecipe() { return recipe; }
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
}