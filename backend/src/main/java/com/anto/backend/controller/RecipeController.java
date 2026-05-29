package com.anto.backend.controller;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.RatingRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.model.Recipe;
import com.anto.backend.service.LogService;
import com.anto.backend.service.MaliciousDetectionService;
import com.anto.backend.service.RecipeService;
import org.springframework.cache.annotation.Cacheable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService service;
    private final LogService logService;
    private final MaliciousDetectionService detectionService;
    public RecipeController(RecipeService service, LogService logService, MaliciousDetectionService detectionService) {
        this.service = service;
        this.logService = logService;
        this.detectionService = detectionService;
    }
    @PostMapping
    public Recipe create(@RequestBody @Valid CreateRecipeRequest request, @RequestParam(required = false) Integer userId, HttpServletRequest httpRequest) {
        Recipe recipe = service.create(request, userId);
        String role = userId != null ? service.getUserRole(userId) : "ANONYMOUS";
        logService.info("CREATE_RECIPE", userId, role, "Created recipe: " + recipe.getName(), httpRequest.getRemoteAddr());
        detectionService.analyze(userId, "CREATE_RECIPE", httpRequest.getRemoteAddr());
        return recipe;
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
    public Recipe update(@PathVariable int id, @RequestBody @Valid UpdateRecipeRequest request, @RequestParam(required = false) Integer userId, HttpServletRequest httpRequest) {
        Recipe recipe = service.update(id, request);
        String role = userId != null ? service.getUserRole(userId) : "ANONYMOUS";
        logService.info("UPDATE_RECIPE", userId, role, "Updated recipe: " + recipe.getName(), httpRequest.getRemoteAddr());
        detectionService.analyze(userId, "UPDATE_RECIPE", httpRequest.getRemoteAddr());
        return recipe;
    }
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable int id, @RequestParam Integer userId, HttpServletRequest httpRequest) {
        String role = service.getUserRole(userId);
        service.deleteById(id, userId);
        logService.info("DELETE_RECIPE", userId, role, "Deleted recipe id: " + id, httpRequest.getRemoteAddr());
        detectionService.analyze(userId, "DELETE_RECIPE", httpRequest.getRemoteAddr());
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

    @GetMapping("/stats/rankings/naive")
    public ResponseEntity<?> naiveRankings() {
        long start = System.currentTimeMillis();
        List<Object[]> results = service.getWeightedRankingsNaive();
        long duration = System.currentTimeMillis() - start;

        List<Map<String, Object>> rankings = results.stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("name", row[1]);
            map.put("category", row[2]);
            map.put("avgRating", row[3]);
            map.put("ratingCount", row[4]);
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "queryTimeMs", duration,
                "count", rankings.size(),
                "rankings", rankings
        ));
    }

    @GetMapping("/stats/rankings/optimized")
     @Cacheable("rankings")
    public ResponseEntity<?> optimizedRankings() {
        long start = System.currentTimeMillis();
        List<Object[]> results = service.getWeightedRankingsNaive();
        long duration = System.currentTimeMillis() - start;
        List<Map<String, Object>> rankings = results.stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("name", row[1]);
            map.put("category", row[2]);
            map.put("avgRating", row[3]);
            map.put("ratingCount", row[4]);
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "queryTimeMs", duration,
                "count", rankings.size(),
                "rankings", rankings
        ));
    }

}
