package com.anto.backend.service;

import com.anto.backend.dto.CreateRecipeRequest;
import com.anto.backend.dto.IngredientRequest;
import com.anto.backend.dto.NutritionalValueRequest;
import com.anto.backend.dto.RecipeUpdateEvent;
import com.github.javafaker.Faker;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RecipeGeneratorService {

    private final RecipeService recipeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private Thread workerThread;

    private final Faker faker = new Faker(new Locale("en"));
    private final Random random = new Random();

    public RecipeGeneratorService(RecipeService recipeService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.recipeService = recipeService;
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized String start(int batchSize, int intervalMillis) {
        if (running.get()) {
            return "Generator is already running";
        }

        running.set(true);

        workerThread = new Thread(() -> {
            while (running.get()) {
                try {
                    for (int i = 0; i < batchSize; i++) {
                        recipeService.create(buildFakeRecipe(), null);
                    }

                    int totalCount = recipeService.getTotalCount();

                    messagingTemplate.convertAndSend(
                            "/topic/recipes",
                            new RecipeUpdateEvent(
                                    "BATCH_CREATED",
                                    batchSize,
                                    totalCount,
                                    "A new batch of recipes was generated"
                            )
                    );

                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    messagingTemplate.convertAndSend(
                            "/topic/recipes",
                            new RecipeUpdateEvent(
                                    "ERROR",
                                    0,
                                    recipeService.getTotalCount(),
                                    "Generator error: " + e.getMessage()
                            )
                    );
                }
            }
        });

        workerThread.setDaemon(true);
        workerThread.start();

        return "Generator started";
    }

    public synchronized String stop() {
        if (!running.get()) {
            return "Generator is not running";
        }

        running.set(false);

        if (workerThread != null) {
            workerThread.interrupt();
        }

        messagingTemplate.convertAndSend(
                "/topic/recipes",
                new RecipeUpdateEvent(
                        "STOPPED",
                        0,
                        recipeService.getTotalCount(),
                        "Generator stopped"
                )
        );

        return "Generator stopped";
    }

    public boolean isRunning() {
        return running.get();
    }

    private CreateRecipeRequest buildFakeRecipe() {
        CreateRecipeRequest request = new CreateRecipeRequest();

        String[] categories = {"Breakfast", "Lunch", "Dinner", "Dessert"};
        String category = categories[random.nextInt(categories.length)];

        request.setName(faker.food().ingredient() + " " + faker.color().name() + " Delight");
        request.setCategory(category);
        request.setServings(random.nextInt(5) + 1);
        request.setPreparationTime((random.nextInt(9) + 1) * 10);
        request.setImage("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400");

        IngredientRequest ing1 = new IngredientRequest();
        ing1.setName(faker.food().ingredient());
        ing1.setQuantity(random.nextInt(5) + 1);
        ing1.setUnit("g");

        IngredientRequest ing2 = new IngredientRequest();
        ing2.setName(faker.food().ingredient());
        ing2.setQuantity(random.nextInt(3) + 1);
        ing2.setUnit("cup");

        IngredientRequest ing3 = new IngredientRequest();
        ing3.setName(faker.food().spice());
        ing3.setQuantity(1);
        ing3.setUnit("tbsp");

        request.setIngredients(List.of(ing1, ing2, ing3));

        request.setSteps(List.of(
                "Prepare all ingredients",
                "Mix the ingredients carefully",
                "Cook for " + (random.nextInt(30) + 10) + " minutes",
                "Serve and enjoy"
        ));

        NutritionalValueRequest cal = new NutritionalValueRequest();
        cal.setName("Calories");
        cal.setAmount(random.nextInt(500) + 150);
        cal.setUnit("kcal");

        NutritionalValueRequest protein = new NutritionalValueRequest();
        protein.setName("Protein");
        protein.setAmount(random.nextInt(25) + 5);
        protein.setUnit("g");

        NutritionalValueRequest carbs = new NutritionalValueRequest();
        carbs.setName("Carbs");
        carbs.setAmount(random.nextInt(60) + 10);
        carbs.setUnit("g");

        request.setNutritionalValues(List.of(cal, protein, carbs));
        return request;
    }
}