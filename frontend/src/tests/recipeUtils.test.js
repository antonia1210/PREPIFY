import { test, expect } from "vitest";
import { addRecipe, updateRecipe, deleteRecipe } from "../utils/recipeUtils";

test("adds a recipe", () => {
    const result = addRecipe([], { name: "Pizza" });
    expect(result.length).toBe(1);
    expect(result[0].name).toBe("Pizza");
});

test("updates a recipe", () => {
    const recipes = [
        { id: 1, name: "Soup" },
        { id: 2, name: "Pizza" },
    ];
    const updated = { id: 2, name: "Updated Pizza" };
    const result = updateRecipe(recipes, updated);
    expect(result).toEqual([
        { id: 1, name: "Soup" },
        { id: 2, name: "Updated Pizza" },
    ]);
});

test("does not update other recipes", () => {
    const recipes = [
        { id: 1, name: "Soup" },
        { id: 2, name: "Pizza" },
    ];
    const updated = { id: 2, name: "Updated Pizza" };
    const result = updateRecipe(recipes, updated);
    expect(result[0]).toEqual({ id: 1, name: "Soup" });
});

test("deletes a recipe", () => {
    const recipes = [{ id: 1 }, { id: 2 }];
    const result = deleteRecipe(recipes, 1);
    expect(result).toEqual([{ id: 2 }]);
});