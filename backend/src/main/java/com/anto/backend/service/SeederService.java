package com.anto.backend.service;

import com.anto.backend.model.*;
import com.anto.backend.repository.*;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SeederService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] CATEGORIES = {
            "Breakfast", "Lunch", "Dinner", "Dessert", "Snack", "Soup", "Salad", "Beverage"
    };

    public SeederService(UserRepository userRepository,
                         RecipeRepository recipeRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Integer> seed(int userCount, int recipeCount) {
        Faker faker = new Faker();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        // 1. Generate users
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            user.setName(firstName + " " + lastName);
            user.setEmail("faker_" + i + "_" + faker.internet().emailAddress());
            user.setUsername("faker_" + i + "_" + faker.internet().username());
            user.setPassword(passwordEncoder.encode("password123"));
            user.setPreferences(CATEGORIES[faker.random().nextInt(CATEGORIES.length)]);
            user.setRoles(List.of(userRole));
            users.add(user);
        }
        userRepository.saveAll(users);

        // 2. Generate recipes
        List<Recipe> recipes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < recipeCount; i++) {
            Recipe recipe = new Recipe();
            recipe.setName(faker.food().dish());
            recipe.setCategory(CATEGORIES[random.nextInt(CATEGORIES.length)]);
            recipe.setServings(random.nextInt(6) + 1);
            recipe.setPreparationTime(random.nextInt(120) + 5);
            recipe.setImage("");

            User author = users.get(random.nextInt(users.size()));
            recipe.setUserId(author.getId());
            recipe.setAuthorName(author.getName());

            // Steps
            int stepCount = random.nextInt(5) + 3;
            List<String> steps = new ArrayList<>();
            for (int s = 0; s < stepCount; s++) {
                steps.add(faker.lorem().sentence());
            }
            recipe.setSteps(steps);

            // Ingredients
            int ingCount = random.nextInt(6) + 3;
            List<Ingredient> ingredients = new ArrayList<>();
            for (int ing = 0; ing < ingCount; ing++) {
                Ingredient ingredient = new Ingredient();
                ingredient.setName(faker.food().ingredient());
                ingredient.setQuantity(random.nextInt(500) + 1);
                ingredient.setUnit(faker.options().option("g", "ml", "cup", "tbsp", "tsp", "pcs"));
                ingredient.setRecipe(recipe);
                ingredients.add(ingredient);
            }
            recipe.setIngredients(ingredients);

            // Nutritional values
            List<NutritionalValue> nutritionalValues = new ArrayList<>();
            for (String nutrient : new String[]{"Calories", "Protein", "Carbs", "Fat"}) {
                NutritionalValue nv = new NutritionalValue();
                nv.setName(nutrient);
                nv.setAmount(random.nextInt(500) + 10);
                nv.setUnit(nutrient.equals("Calories") ? "kcal" : "g");
                nv.setRecipe(recipe);
                nutritionalValues.add(nv);
            }
            recipe.setNutritionalValues(nutritionalValues);

            // Ratings — each recipe gets 10-50 random ratings (many-to-many simulation)
            int ratingCount = random.nextInt(41) + 10;
            List<Integer> ratings = new ArrayList<>();
            for (int r = 0; r < ratingCount; r++) {
                ratings.add(random.nextInt(5) + 1);
            }
            recipe.setRatings(ratings);
            recipes.add(recipe);
        }
        recipeRepository.saveAll(recipes);

        return Map.of(
                "users", userCount,
                "recipes", recipeCount,
                "ratings", recipes.stream().mapToInt(r -> r.getRatings().size()).sum()
        );
    }
}