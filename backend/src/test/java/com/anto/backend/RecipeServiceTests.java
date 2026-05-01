package com.anto.backend;


import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.exception.NotFoundException;
import com.anto.backend.model.Recipe;
import com.anto.backend.repository.InMemoryRecipeRepository;
import com.anto.backend.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeServiceTests {

    private RecipeService service;

    @BeforeEach
    void setUp() {
        service = new RecipeService(new InMemoryRecipeRepository());
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
        Recipe r = service.create(makeRequest("Pancakes", "Breakfast"));
        assertNotNull(r.getId());
        assertEquals("Pancakes", r.getName());
    }

    @Test
    void testGetById() {
        Recipe created = service.create(makeRequest("Soup", "Dinner"));
        Recipe found = service.getById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById(999));
    }

    @Test
    void testGetAll() {
        service.create(makeRequest("A", "Breakfast"));
        service.create(makeRequest("B", "Lunch"));
        List<Recipe> page = service.getAll(0, 2);
        assertEquals(2, page.size());
    }

    @Test
    void testGetAllPagination() {
        service.create(makeRequest("A", "Breakfast"));
        service.create(makeRequest("B", "Lunch"));
        service.create(makeRequest("C", "Dinner"));
        List<Recipe> page = service.getAll(1, 2);
        assertEquals(1, page.size());
    }

    @Test
    void testGetAllEmptyPage() {
        List<Recipe> page = service.getAll(5, 10);
        assertTrue(page.isEmpty());
    }

    @Test
    void testUpdate() {
        Recipe created = service.create(makeRequest("Old", "Breakfast"));
        UpdateRecipeRequest req = new UpdateRecipeRequest();
        req.setName("New");
        req.setCategory("Dinner");
        req.setServings(4);
        req.setPreparationTime(30);
        req.setImage("new.jpg");
        req.setIngredients(List.of("flour"));
        req.setSteps(List.of("bake"));
        req.setNutritionalValues(List.of("200kcal"));
        Recipe updated = service.update(created.getId(), req);
        assertEquals("New", updated.getName());
    }

    @Test
    void testDelete() {
        Recipe created = service.create(makeRequest("ToDelete", "Lunch"));
        service.deleteById(created.getId());
        assertThrows(NotFoundException.class, () -> service.getById(created.getId()));
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(NotFoundException.class, () -> service.deleteById(999));
    }

    @Test
    void testAddRating() {
        Recipe created = service.create(makeRequest("Cake", "Dessert"));
        service.addRating(created.getId(), 5);
        Recipe found = service.getById(created.getId());
        assertEquals(5.0, found.getAverageRating());
    }

    @Test
    void testAddRatingNotFound() {
        assertThrows(NotFoundException.class, () -> service.addRating(999, 3));
    }

    @Test
    void testSearch() {
        service.create(makeRequest("Pancakes", "Breakfast"));
        service.create(makeRequest("Soup", "Dinner"));
        List<Recipe> results = service.search("Pancakes");
        assertEquals(1, results.size());
    }

    @Test
    void testFilter() {
        service.create(makeRequest("Pancakes", "Breakfast"));
        service.create(makeRequest("Soup", "Dinner"));
        List<Recipe> results = service.filter("Breakfast", null);
        assertEquals(1, results.size());
    }

    @Test
    void testFilterSortByPrepTime() {
        service.create(makeRequest("A", "Breakfast"));
        service.create(makeRequest("B", "Breakfast"));
        List<Recipe> results = service.filter(null, "prepTime");
        assertEquals(2, results.size());
    }

    @Test
    void testGetCountByCategories() {
        service.create(makeRequest("Pancakes", "Breakfast"));
        service.create(makeRequest("Soup", "Dinner"));
        var map = service.getCountByCategories();
        assertEquals(1L, map.get("Breakfast"));
    }

    @Test
    void testGetAverageRating() {
        Recipe r = service.create(makeRequest("Cake", "Dessert"));
        service.addRating(r.getId(), 4);
        service.addRating(r.getId(), 2);
        assertEquals(3.0, service.getAverageRating());
    }
}
