package com.anto.backend.controller;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.model.Recipe;
import com.anto.backend.service.RecipeGeneratorService;
import com.anto.backend.service.RecipeService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RecipeGraphQLController {

    private final RecipeService recipeService;
    private final RecipeGeneratorService recipeGeneratorService;

    public RecipeGraphQLController(RecipeService recipeService, RecipeGeneratorService recipeGeneratorService) {
        this.recipeService = recipeService;
        this.recipeGeneratorService = recipeGeneratorService;
    }

    @QueryMapping
    public List<Recipe> recipes(@Argument int page, @Argument int size) {
        return recipeService.getAll(page, size);
    }

    @QueryMapping
    public Recipe recipe(@Argument int id) {
        return recipeService.getById(id);
    }

    @QueryMapping
    public List<Recipe> searchRecipes(@Argument String query) {
        return recipeService.search(query);
    }

    @QueryMapping
    public List<Recipe> filterRecipes(@Argument String category, @Argument String sortBy) {
        return recipeService.filter(category, sortBy);
    }

    @QueryMapping
    public List<Map<String, Object>> recipeCountByCategory() {
        return recipeService.getCountByCategories().entrySet().stream()
                .map(e -> Map.of("name", (Object) e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public int totalCount() {
        return recipeService.getTotalCount();
    }

    @QueryMapping
    public double averageRating() {
        return recipeService.getAverageRating();
    }

    @MutationMapping
    public Recipe createRecipe(
            @Argument String name, @Argument String category,
            @Argument int servings, @Argument int preparationTime,
            @Argument String image, @Argument List<String> ingredients,
            @Argument List<String> steps, @Argument List<String> nutritionalValues) {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setName(name);
        request.setCategory(category);
        request.setServings(servings);
        request.setPreparationTime(preparationTime);
        request.setImage(image);
        request.setIngredients(ingredients);
        request.setSteps(steps);
        request.setNutritionalValues(nutritionalValues);
        return recipeService.create(request);
    }

    @MutationMapping
    public Recipe updateRecipe(
            @Argument int id, @Argument String name, @Argument String category,
            @Argument int servings, @Argument int preparationTime,
            @Argument String image, @Argument List<String> ingredients,
            @Argument List<String> steps, @Argument List<String> nutritionalValues) {
        UpdateRecipeRequest request = new UpdateRecipeRequest();
        request.setName(name);
        request.setCategory(category);
        request.setServings(servings);
        request.setPreparationTime(preparationTime);
        request.setImage(image);
        request.setIngredients(ingredients);
        request.setSteps(steps);
        request.setNutritionalValues(nutritionalValues);
        return recipeService.update(id, request);
    }

    @MutationMapping
    public boolean deleteRecipe(@Argument int id) {
        recipeService.deleteById(id);
        return true;
    }

    @MutationMapping
    public Recipe addRating(@Argument int id, @Argument int rating) {
        return recipeService.addRating(id, rating);
    }

    @MutationMapping
    public boolean startFaker() {
        recipeGeneratorService.start(3,3000);
        return true;
    }

    @MutationMapping
    public boolean stopFaker() {
        recipeGeneratorService.stop();
        return true;
    }
}