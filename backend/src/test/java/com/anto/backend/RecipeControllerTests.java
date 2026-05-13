package com.anto.backend;

import com.anto.backend.controller.RecipeController;
import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.IngredientRequest;
import com.anto.backend.dto.NutritionalValueRequest;
import com.anto.backend.dto.RatingRequest;
import com.anto.backend.dto.UpdateRecipeRequest;
import com.anto.backend.model.Recipe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RecipeControllerTests {

    @Autowired
    private RecipeController controller;

    private final MockHttpServletRequest mockRequest = new MockHttpServletRequest();

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
        Recipe r = controller.create(makeRequest("Pancakes", "Breakfast"), null, mockRequest);
        assertNotNull(r.getId());
        assertEquals("Pancakes", r.getName());
    }

    @Test
    void testGetAll() {
        controller.create(makeRequest("Pancakes", "Breakfast"), null, mockRequest);
        List<Recipe> result = controller.getAll(0, 10);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetById() {
        Recipe created = controller.create(makeRequest("Soup", "Dinner"), null, mockRequest);
        Recipe found = controller.getById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testUpdate() {
        Recipe created = controller.create(makeRequest("Old", "Breakfast"), null, mockRequest);
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
        Recipe updated = controller.update(created.getId(), req, null, mockRequest);
        assertEquals("New", updated.getName());
    }

    @Test
    void testDelete() {
        Recipe created = controller.create(makeRequest("ToDelete", "Lunch"), 1, mockRequest);
        controller.deleteById(created.getId(), 1, mockRequest);
        assertThrows(Exception.class, () -> controller.getById(created.getId()));
    }

    @Test
    void testAddRating() {
        Recipe created = controller.create(makeRequest("Cake", "Dessert"), null, mockRequest);
        RatingRequest rating = new RatingRequest();
        rating.setRating(5);
        Recipe rated = controller.addRating(created.getId(), rating);
        assertEquals(5.0, rated.getAverageRating());
    }

    @Test
    void testSearch() {
        controller.create(makeRequest("Pancakes", "Breakfast"), null, mockRequest);
        List<Recipe> results = controller.search("Pancakes");
        assertFalse(results.isEmpty());
    }

    @Test
    void testFilter() {
        controller.create(makeRequest("Pancakes", "Breakfast"), null, mockRequest);
        controller.create(makeRequest("Soup", "Dinner"), null, mockRequest);
        List<Recipe> results = controller.filter("Breakfast", null);
        assertFalse(results.isEmpty());
    }

    @Test
    void testStats() {
        controller.create(makeRequest("Pancakes", "Breakfast"), null, mockRequest);
        assertNotNull(controller.getCountByCategories());
        assertTrue(controller.getTotalCount() > 0);
    }
}