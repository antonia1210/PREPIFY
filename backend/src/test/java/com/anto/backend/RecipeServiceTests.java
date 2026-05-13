package com.anto.backend;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.IngredientRequest;
import com.anto.backend.dto.NutritionalValueRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.exception.NotFoundException;
import com.anto.backend.model.Recipe;
import com.anto.backend.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RecipeServiceTests {

    @Autowired
    private RecipeService service;

    private CreateRecipeRequest makeRequest(String name, String category) {
        CreateRecipeRequest r = new CreateRecipeRequest();
        r.setName(name);
        r.setCategory(category);
        r.setServings(2);
        r.setPreparationTime(10);
        r.setImage("img.jpg");

        IngredientRequest ing = new IngredientRequest();
        ing.setName("egg");
        ing.setQuantity(1);
        ing.setUnit("piece");
        r.setIngredients(List.of(ing));

        NutritionalValueRequest nutr = new NutritionalValueRequest();
        nutr.setName("Calories");
        nutr.setAmount(100);
        nutr.setUnit("kcal");
        r.setNutritionalValues(List.of(nutr));

        r.setSteps(List.of("cook"));
        return r;
    }

    @Test
    void testCreate() {
        Recipe r = service.create(makeRequest("Pancakes", "Breakfast"), null);
        assertNotNull(r.getId());
        assertEquals("Pancakes", r.getName());
    }

    @Test
    void testGetById() {
        Recipe created = service.create(makeRequest("Soup", "Dinner"), null);
        Recipe found = service.getById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById(999));
    }

    @Test
    void testGetAll() {
        service.create(makeRequest("A", "Breakfast"), null);
        service.create(makeRequest("B", "Lunch"), null);
        List<Recipe> page = service.getAll(0, 2);
        assertFalse(page.isEmpty());
    }

    @Test
    void testGetAllEmptyPage() {
        List<Recipe> page = service.getAll(999, 10);
        assertTrue(page.isEmpty());
    }

    @Test
    void testUpdate() {
        Recipe created = service.create(makeRequest("Old", "Breakfast"), null);
        UpdateRecipeRequest req = new UpdateRecipeRequest();
        req.setName("New");
        req.setCategory("Dinner");
        req.setServings(4);
        req.setPreparationTime(30);
        req.setImage("new.jpg");

        IngredientRequest ing = new IngredientRequest();
        ing.setName("flour");
        ing.setQuantity(2);
        ing.setUnit("cup");
        req.setIngredients(List.of(ing));

        NutritionalValueRequest nutr = new NutritionalValueRequest();
        nutr.setName("Calories");
        nutr.setAmount(200);
        nutr.setUnit("kcal");
        req.setNutritionalValues(List.of(nutr));

        req.setSteps(List.of("bake"));
        Recipe updated = service.update(created.getId(), req);
        assertEquals("New", updated.getName());
    }

    @Test
    void testDelete() {
        Recipe created = service.create(makeRequest("ToDelete", "Lunch"), 1);
        service.deleteById(created.getId(), 1);
        assertThrows(NotFoundException.class, () -> service.getById(created.getId()));
    }

    @Test
    void testDeleteNotFound() {
        assertThrows(NotFoundException.class, () -> service.deleteById(999, 1));
    }

    @Test
    void testAddRating() {
        Recipe created = service.create(makeRequest("Cake", "Dessert"), null);
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
        service.create(makeRequest("Pancakes", "Breakfast"), null);
        List<Recipe> results = service.search("Pancakes");
        assertFalse(results.isEmpty());
    }

    @Test
    void testFilter() {
        service.create(makeRequest("Pancakes", "Breakfast"), null);
        service.create(makeRequest("Soup", "Dinner"), null);
        List<Recipe> results = service.filter("Breakfast", null);
        assertFalse(results.isEmpty());
    }

    @Test
    void testFilterSortByPrepTime() {
        service.create(makeRequest("A", "Breakfast"), null);
        service.create(makeRequest("B", "Breakfast"), null);
        List<Recipe> results = service.filter(null, "prepTime");
        assertFalse(results.isEmpty());
    }

    @Test
    void testGetCountByCategories() {
        service.create(makeRequest("Pancakes", "Breakfast"), null);
        service.create(makeRequest("Soup", "Dinner"), null);
        var map = service.getCountByCategories();
        assertNotNull(map);
        assertTrue(map.containsKey("Breakfast"));
    }

    @Test
    void testGetAverageRating() {
        Recipe r = service.create(makeRequest("Cake", "Dessert"), null);
        service.addRating(r.getId(), 4);
        service.addRating(r.getId(), 2);
        assertTrue(service.getAverageRating() > 0);
    }
}