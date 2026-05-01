package com.anto.backend.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CreateRecipeRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Category cannot be blank")
    private String category;
    @NotNull(message = "Servings cannot be null")
    @Min(value = 1, message = "Servings must be greater than or equal to 1")
    private Integer servings;
    @NotNull(message = "Preparation time cannot be null")
    @Min(value = 1, message = "Preparation time must be greater than or equal to 1")
    private Integer preparationTime;
    private String image;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> nutritionalValues;
    public CreateRecipeRequest() {}

    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public Integer getServings() {
        return servings;
    }
    public Integer getPreparationTime() {
        return preparationTime;
    }
    public String getImage() {
        return image;
    }
    public List<String> getIngredients() {
        return ingredients;
    }
    public List<String> getSteps() {
        return steps;
    }
    public List<String> getNutritionalValues() {
        return nutritionalValues;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setServings(Integer servings) {
        this.servings = servings;
    }
    public void setPreparationTime(Integer preparationTime) {
        this.preparationTime = preparationTime;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
    public void setNutritionalValues(List<String> nutritionalValues) {
        this.nutritionalValues = nutritionalValues;
    }
}
