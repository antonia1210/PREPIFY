package com.anto.backend;

import com.anto.backend.controller.RecipeController;
import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.RatingRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.model.Recipe;
import com.anto.backend.repository.InMemoryRecipeRepository;
import com.anto.backend.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeControllerTests {

    private RecipeController controller;

    @BeforeEach
    void setUp() {
        RecipeService service = new RecipeService(new InMemoryRecipeRepository());
        controller = new RecipeController(service);
    }

    private CreateRecipeRequest makeRequest(String name, String category) {
        CreateRecipeRequest r = new CreateRecipeRequest();
        r.setName(name);
        r.setCategory(category);
        r.setServings(2);
        r.setPreparationTime(10);
        r.setImage("img.jpg");
        r.setIngredients(List.of("egg"));
        r.setSteps(List.of("cook"));
        r.setNutritionalValues(List.of("100kcal"));
        return r;
    }

    @Test
    void testCreate() {
        Recipe r = controller.create(makeRequest("Pancakes", "Breakfast"));
        assertNotNull(r.getId());
        assertEquals("Pancakes", r.getName());
    }

    @Test
    void testGetAll() {
        controller.create(makeRequest("Pancakes", "Breakfast"));
        List<Recipe> result = controller.getAll(0, 10);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetById() {
        Recipe created = controller.create(makeRequest("Soup", "Dinner"));
        Recipe found = controller.getById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testUpdate() {
        Recipe created = controller.create(makeRequest("Old", "Breakfast"));
        UpdateRecipeRequest req = new UpdateRecipeRequest();
        req.setName("New");
        req.setCategory("Dinner");
        req.setServings(4);
        req.setPreparationTime(30);
        req.setImage("new.jpg");
        req.setIngredients(List.of("flour"));
        req.setSteps(List.of("bake"));
        req.setNutritionalValues(List.of("200kcal"));
        Recipe updated = controller.update(created.getId(), req);
        assertEquals("New", updated.getName());
    }

    @Test
    void testDelete() {
        Recipe created = controller.create(makeRequest("ToDelete", "Lunch"));
        controller.deleteById(created.getId());
        assertThrows(Exception.class, () -> controller.getById(created.getId()));
    }

    @Test
    void testAddRating() {
        Recipe created = controller.create(makeRequest("Cake", "Dessert"));
        RatingRequest rating = new RatingRequest();
        rating.setRating(5);
        Recipe rated = controller.addRating(created.getId(), rating);
        assertEquals(5.0, rated.getAverageRating());
    }

    @Test
    void testSearch() {
        controller.create(makeRequest("Pancakes", "Breakfast"));
        List<Recipe> results = controller.search("Pancakes");
        assertEquals(1, results.size());
    }

    @Test
    void testFilter() {
        controller.create(makeRequest("Pancakes", "Breakfast"));
        controller.create(makeRequest("Soup", "Dinner"));
        List<Recipe> results = controller.filter("Breakfast", null);
        assertEquals(1, results.size());
    }

    @Test
    void testStats() {
        controller.create(makeRequest("Pancakes", "Breakfast"));
        assertNotNull(controller.getCountByCategories());
        assertTrue(controller.getTotalCount() > 0);
        assertEquals(0.0, controller.getAverageRating());
    }
}