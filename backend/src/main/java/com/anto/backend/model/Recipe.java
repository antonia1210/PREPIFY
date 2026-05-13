package com.anto.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String category;
    private int servings;
    private int preparationTime;
    private String image;
    private String authorName;

    @Column(name = "user_id")
    private Integer userId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_steps", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "step")
    private List<String> steps = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ratings", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "rating")
    private List<Integer> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<NutritionalValue> nutritionalValues = new ArrayList<>();

    public Recipe() {}
    public Recipe(Integer id, String name, String category, int servings, int preparationTime, String image, List<Ingredient> ingredients, List<String> steps, List<NutritionalValue> nutritionalValues, Integer userId, String authorName)
    {
        this.id = id;
        this.name = name;
        this.category = category;
        this.servings = servings;
        this.preparationTime = preparationTime;
        this.image = image;
        this.ingredients = ingredients;
        this.steps = steps;
        this.nutritionalValues = nutritionalValues;
        this.userId = userId;
        this.authorName = authorName;
    }
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public int getServings() {
        return servings;
    }
    public int getPreparationTime() {
        return preparationTime;
    }
    public String getImage() {
        return image;
    }
    public List<Ingredient> getIngredients() { return ingredients; }
    public List<String> getSteps() {
        return steps;
    }
    public List<NutritionalValue> getNutritionalValues() {
        return nutritionalValues;
    }
    public List<Integer> getRatings() {return ratings;}
    public void setRatings(List<Integer> ratings) {this.ratings = ratings;}
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCategory(String category) {this.category = category;}
    public void setServings(int servings) {
        this.servings = servings;
    }
    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
    }
    public void setSteps(List<String> steps) {
        this.steps = new ArrayList<>(steps);
    }
    public void setNutritionalValues(List<NutritionalValue> nutritionalValues) {
        this.nutritionalValues = new ArrayList<>(nutritionalValues);
    }

    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.ratings = new ArrayList<>(this.ratings);
        this.ratings.add(rating);
    }

    public double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) return 0;
        return ratings.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }
}
