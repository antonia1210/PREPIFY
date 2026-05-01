package com.anto.backend.service;

import com.anto.backend.dto.CreateRecipeRequest;
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
                        recipeService.create(buildFakeRecipe());
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

        request.setIngredients(List.of(
                faker.food().ingredient(),
                faker.food().ingredient(),
                faker.food().ingredient(),
                faker.food().spice()
        ));

        request.setSteps(List.of(
                "Prepare all ingredients",
                "Mix the ingredients carefully",
                "Cook for " + (random.nextInt(30) + 10) + " minutes",
                "Serve and enjoy"
        ));

        request.setNutritionalValues(List.of(
                "Calories: " + (random.nextInt(500) + 150),
                "Protein: " + (random.nextInt(25) + 5) + "g",
                "Carbs: " + (random.nextInt(60) + 10) + "g"
        ));

        return request;
    }
}