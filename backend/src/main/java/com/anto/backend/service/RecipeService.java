package com.anto.backend.service;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.exception.NotFoundException;
import com.anto.backend.model.Recipe;
import com.anto.backend.repository.InMemoryRecipeRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    private final InMemoryRecipeRepository recipeRepository;
    public RecipeService(InMemoryRecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }
    public Recipe create(CreateRecipeRequest request){
        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setCategory(request.getCategory());
        recipe.setServings(request.getServings());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setImage(request.getImage());
        recipe.setIngredients(request.getIngredients());
        recipe.setSteps(request.getSteps());
        recipe.setNutritionalValues(request.getNutritionalValues());
        return recipeRepository.save(recipe);
    }
    public Recipe update(int id, UpdateRecipeRequest request){
        Recipe recipe = recipeRepository.findById(id);
        recipe.setName(request.getName());
        recipe.setCategory(request.getCategory());
        recipe.setServings(request.getServings());
        recipe.setPreparationTime(request.getPreparationTime());
        recipe.setImage(request.getImage());
        recipe.setIngredients(request.getIngredients());
        recipe.setSteps(request.getSteps());
        recipe.setNutritionalValues(request.getNutritionalValues());
        return recipeRepository.save(recipe);
    }
    public Recipe getById(int id){
        Recipe recipe = recipeRepository.findById(id);
        if(recipe == null){
            throw new NotFoundException(id);
        }
        return recipe;
    }
    public List<Recipe> getAll(int page, int size){
        List<Recipe> recipes = recipeRepository.findAll();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, recipes.size());
        if (startIndex >= recipes.size()){
            return List.of();
        }
        return recipes.subList(startIndex, endIndex);
    }
    public void deleteById(int id){
        if(!recipeRepository.existsById(id)){
            throw new NotFoundException(id);
        }
        recipeRepository.deleteById(id);
    }
    public Recipe addRating(Integer id, int rating)
    {
        Recipe recipe = recipeRepository.findById(id);
        if(recipe == null) {
            throw new NotFoundException(id);
        }
        recipe.addRating(rating);
        return recipeRepository.save(recipe);
    }
    public Map<String, Long> getCountByCategories(){
        return recipeRepository.findAll().stream().collect(Collectors.groupingBy(Recipe::getCategory, Collectors.counting()));
    }
    public Integer getTotalCount(){
        return recipeRepository.findAll().size();
    }
    public double getAverageRating(){
        return recipeRepository.findAll().stream().
                flatMap(r->r.getRatings().stream()).mapToInt(Integer::intValue).average().orElse(0);
    }
    public List<Recipe> search(String query){
        return recipeRepository.search(query);
    }
    public List<Recipe> filter(String category, String sortBy){
        return recipeRepository.filter(category, sortBy);
    }

    public List<Recipe> getByUserId(int userId){
        return recipeRepository.findAll().stream().filter(r->r.getUserId().equals(userId)).toList();
    }
    public long countForUserId(int userId){
        return recipeRepository.findAll().stream().filter(r->r.getUserId().equals(userId)).count();
    }
}
