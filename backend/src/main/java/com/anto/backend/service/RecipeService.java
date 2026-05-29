package com.anto.backend.service;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.exception.NotFoundException;
import com.anto.backend.model.Ingredient;
import com.anto.backend.model.NutritionalValue;
import com.anto.backend.model.Recipe;
import com.anto.backend.model.User;
import com.anto.backend.repository.RecipeRepository;
import com.anto.backend.repository.UserRepository;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    public RecipeService(RecipeRepository recipeRepository, UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }
    public Recipe create(CreateRecipeRequest request, Integer userId) {
        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setCategory(request.getCategory());
        recipe.setServings(request.getServings());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setImage(request.getImage());
        recipe.setUserId(userId);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> recipe.setAuthorName(u.getName()));
        }
        List<Ingredient> ingredients = request.getIngredients().stream()
                .map(i -> new Ingredient(i.getName(), i.getQuantity(), i.getUnit(), recipe))
                .collect(Collectors.toList());
        recipe.setIngredients(ingredients);
        recipe.setSteps(request.getSteps());
        List<NutritionalValue> nutritionalValues = request.getNutritionalValues().stream()
                .map(n -> new NutritionalValue(n.getName(), n.getAmount(), n.getUnit(), recipe))
                .collect(Collectors.toList());
        recipe.setNutritionalValues(nutritionalValues);
        return recipeRepository.save(recipe);
    }

    public Recipe update(int id, UpdateRecipeRequest request){
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
        recipe.setName(request.getName());
        recipe.setCategory(request.getCategory());
        recipe.setServings(request.getServings());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setImage(request.getImage());
        recipe.setIngredients(new ArrayList<>(request.getIngredients().stream()
                .map(i -> new Ingredient(i.getName(), i.getQuantity(), i.getUnit(), recipe))
                .collect(Collectors.toList())));
        recipe.setNutritionalValues(new ArrayList<>(request.getNutritionalValues().stream()
                .map(n -> new NutritionalValue(n.getName(), n.getAmount(), n.getUnit(), recipe))
                .collect(Collectors.toList())));
        return recipeRepository.save(recipe);
    }
    public Recipe getById(int id){
        return recipeRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    }
    public List<Recipe> getAll(int page, int size){
        return recipeRepository.findAll(
                org.springframework.data.domain.PageRequest.of(page, size)
        ).getContent();
    }
    public void deleteById(int id, Integer requestingUserId) {
        Recipe recipe = getById(id);
        User user = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));
        boolean isOwner = recipe.getUserId() != null &&
                recipe.getUserId().equals(requestingUserId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Not authorized to delete this recipe");
        }
        recipeRepository.deleteById(id);
    }
    public Recipe addRating(Integer id, int rating)
    {
        Recipe recipe = getById(id);
        recipe.addRating(rating);
        return recipeRepository.save(recipe);
    }
    public Map<String, Long> getCountByCategories(){
         return recipeRepository.countByCategory().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }
    public Integer getTotalCount(){
        return (int) recipeRepository.count();
    }
    public double getAverageRating(){
        return recipeRepository.findAll().stream().
                flatMap(r->r.getRatings().stream()).mapToInt(Integer::intValue).average().orElse(0);
    }
    public List<Recipe> search(String query){
        if (query == null || query.isBlank()) return recipeRepository.findAll();
        return recipeRepository.search(query);
    }

    private Comparator<Recipe> getComparator(String sortBy) {
        if (sortBy == null) return Comparator.comparing(Recipe::getName);
        return switch (sortBy) {
            case "rating" -> Comparator.comparingDouble(Recipe::getAverageRating).reversed();
            case "prepTime" -> Comparator.comparingInt(Recipe::getPreparationTime);
            case "servings" -> Comparator.comparingInt(Recipe::getServings);
            default -> Comparator.comparing(Recipe::getName);
        };
    }

    public List<Recipe> filter(String category, String sortBy){
        List<Recipe> recipes = category != null ?
                recipeRepository.findByCategory(category) :
                recipeRepository.findAll();
        return recipes.stream().sorted(getComparator(sortBy)).collect(Collectors.toList());
    }

    public List<Recipe> getByUserId(int userId){
        return recipeRepository.findByUserId(userId);
    }
    public long countForUserId(int userId){
        return recipeRepository.countByUserId(userId);
    }
    public String getUserRole(Integer userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.joining(",")))
                .orElse("UNKNOWN");
    }
    public List<Object[]> getWeightedRankingsNaive() {
        return recipeRepository.getWeightedRankingsNaive();
    }
}
