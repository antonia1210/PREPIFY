package com.anto.backend.repository;

import com.anto.backend.model.Recipe;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryRecipeRepository {
    private final Map<Integer, Recipe> recipesData = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    @jakarta.annotation.PostConstruct
    public void init() {
        save(new Recipe(null, "Pancakes", "Breakfast", 4, 15, "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400",
                List.of("2 cups flour", "2 eggs", "1 cup milk", "2 tbsp butter"),
                List.of("Mix dry ingredients", "Add wet ingredients", "Cook on pan"),
                List.of("Calories: 350", "Protein: 8g", "Carbs: 45g"),1, "Alice"));

        save(new Recipe(null, "Caesar Salad", "Lunch", 2, 10, "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400",
                List.of("Romaine lettuce", "Croutons", "Parmesan", "Caesar dressing"),
                List.of("Wash lettuce", "Add toppings", "Toss with dressing"),
                List.of("Calories: 200", "Protein: 6g", "Carbs: 15g"),1, "Alice"));

        save(new Recipe(null, "Spaghetti Bolognese", "Dinner", 4, 45, "https://images.unsplash.com/photo-1598866594230-a7c12756260f?w=400",
                List.of("400g spaghetti", "300g ground beef", "Tomato sauce", "Onion", "Garlic"),
                List.of("Cook pasta", "Brown the meat", "Add sauce", "Combine and serve"),
                List.of("Calories: 600", "Protein: 30g", "Carbs: 70g"),1, "Alice"));

        save(new Recipe(null, "Chocolate Cake", "Dessert", 8, 60, "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400",
                List.of("2 cups flour", "1 cup cocoa", "2 cups sugar", "3 eggs", "1 cup butter"),
                List.of("Mix ingredients", "Pour in pan", "Bake at 180C for 35 min", "Cool and frost"),
                List.of("Calories: 450", "Protein: 5g", "Carbs: 60g"),2, "Hannah"));

        save(new Recipe(null, "Omelette", "Breakfast", 1, 10, "https://images.unsplash.com/photo-1510693206972-df098062cb71?w=400",
                List.of("3 eggs", "Salt", "Pepper", "Butter", "Cheese"),
                List.of("Beat eggs", "Pour in pan", "Add cheese", "Fold and serve"),
                List.of("Calories: 250", "Protein: 18g", "Carbs: 2g"),2, "Hannah"));

        save(new Recipe(null, "Chicken Soup", "Dinner", 6, 90, "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400",
                List.of("1 whole chicken", "Carrots", "Celery", "Onion", "Salt", "Pepper"),
                List.of("Boil chicken", "Add vegetables", "Season and simmer"),
                List.of("Calories: 300", "Protein: 25g", "Carbs: 20g"),2, "Hannah"));
    }
    public Recipe save(Recipe recipe) {
        if(recipe.getId() == null) {
            recipe.setId(idGenerator.getAndIncrement());
        }
        recipesData.put(recipe.getId(), recipe);
        return recipe;
    }
    public Recipe findById(int id) {
        return recipesData.get(id);
    }
    public List<Recipe> findAll() {
        return List.copyOf(recipesData.values());
    }
    public void deleteById(int id) {
        recipesData.remove(id);
    }
    public boolean existsById(int id) {
        return recipesData.containsKey(id);
    }
    public List<Recipe> search(String query) {
        return recipesData.values().stream()
                .filter(r -> r.getName().toLowerCase().contains(query.toLowerCase()) ||
                        r.getCategory().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }
    private Comparator<Recipe> getComparator(String sortBy) {
        if ("rating".equals(sortBy)) {
            return Comparator.comparingDouble(Recipe::getAverageRating).reversed();
        } else if ("prepTime".equals(sortBy)) {
            return Comparator.comparingInt(Recipe::getPreparationTime);
        }else if("servings".equals(sortBy)){
            return Comparator.comparingInt(Recipe::getServings);
        }
        return Comparator.comparing(Recipe::getName);
    }
    public List<Recipe> filter(String category, String sortBy){
        return recipesData.values().stream()
                .filter(r -> category == null || r.getCategory().equalsIgnoreCase(category))
                .sorted(getComparator(sortBy))
                .toList();
    }
}