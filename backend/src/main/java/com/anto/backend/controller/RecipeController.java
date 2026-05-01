package com.anto.backend.controller;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.RatingRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.model.Recipe;
import com.anto.backend.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService service;
    public RecipeController(RecipeService service) {
        this.service = service;
    }
    @PostMapping
    public Recipe create(@RequestBody @Valid CreateRecipeRequest request){
        return service.create(request);
    }
    @GetMapping("/search")
    public List<Recipe> search(@RequestParam(required = false) String query){
        if (query == null || query.isEmpty()) return service.getAll(0, 10);
        return service.search(query);
    }
    @GetMapping("/filter")
    public List<Recipe> filter(@RequestParam(required = false) String category, @RequestParam(required = false) String sortBy){
        return service.filter(category, sortBy);
    }
    @GetMapping("/{id}")
    public Recipe getById(@PathVariable int id){
        return service.getById(id);
    }
    @GetMapping
    public List<Recipe> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return service.getAll(page, size);
    }
    @PutMapping("/{id}")
    public Recipe update(@PathVariable int id, @RequestBody @Valid UpdateRecipeRequest request){
        return service.update(id, request);
    }
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable int id){
        service.deleteById(id);
    }
    @PostMapping("/{id}/rating")
    public Recipe addRating(@PathVariable int id, @RequestBody @Valid RatingRequest request){
        return service.addRating(id, request.getRating());
    }
    @GetMapping("/stats/by-category")
    public Map<String, Long> getCountByCategories(){
        return service.getCountByCategories();
    }
    @GetMapping("/stats/total-count")
    public Integer getTotalCount(){
        return service.getTotalCount();
    }
    @GetMapping("/stats/average-rating")
    public double getAverageRating(){
        return service.getAverageRating();
    }
    @GetMapping("/user/{userId}")
    public List<Recipe> getByUserId(@PathVariable int userId) {
        return service.getByUserId(userId);
    }
    @GetMapping("/user/{userId}/count")
    public long countForUserId(@PathVariable int userId) {
        return service.countForUserId(userId);
    }

}
