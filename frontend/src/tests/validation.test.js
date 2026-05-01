import { validateRecipe } from "../validation/recipeValidation.js";
import { validateLogin } from "../validation/loginValidation.js";
import { validateRegister } from "../validation/registerValidation.js";

import { test, expect } from "vitest";

test("recipe validation - empty fields", () => {
    const result = validateRecipe({
        name: "",
        category: "",
        servings: "",
    });

    expect(result).toEqual({
        name: "Name cannot be empty field",
        category: "Category cannot be empty field",
        servings: "Servings cannot be empty field",
    });
});

test("recipe validation - negative servings", () => {
    const result = validateRecipe({
        name: "Pizza",
        category: "Dinner",
        servings: -1,
    });

    expect(result.servings).toBe("Servings cannot be negative");
});

test("recipe validation - negative preparation time", () => {
    const result = validateRecipe({
        name: "Pizza",
        category: "Dinner",
        servings: 2,
        preparationTime: -5,
    });
    expect(result.preparationTime).toBe("Preparation time cannot be negative");
});

test("recipe validation - valid input", () => {
    const result = validateRecipe({
        name: "Soup",
        category: "Lunch",
        servings: 2,
    });

    expect(result).toEqual({});
});

test("login validation - empty", () => {
    const result = validateLogin({
        email: "",
        password: "",
    });

    expect(result).toEqual({
        email: "Email is required",
        password: "Password is required",
    });
});
test("login validation - only email missing", () => {
    const result = validateLogin({
        email: "",
        password: "password123",
    });
    expect(result).toEqual({ email: "Email is required" });
});

test("login validation - only password missing", () => {
    const result = validateLogin({
        email: "test@test.com",
        password: "",
    });
    expect(result).toEqual({ password: "Password is required" });
});

test("register validation - weak password", () => {
    const result = validateRegister({
        name: "Ana",
        username: "ana123",
        email: "ana@email.com",
        password: "abc",
        repeatPassword: "abc",
    });

    expect(result.password).toBeDefined();
});

test("register validation - invalid email", () => {
    const result = validateRegister({
        name: "Ana",
        username: "ana123",
        email: "invalid-email",
        password: "Strong1!",
        repeatPassword: "Strong1!",
    });

    expect(result.email).toBe("Invalid email format");
});

test("register validation - password mismatch", () => {
    const result = validateRegister({
        name: "Ana",
        username: "ana123",
        email: "ana@email.com",
        password: "Strong1!",
        repeatPassword: "Different1!",
    });

    expect(result.repeatPassword).toBe("Passwords do not match");
});

test("register validation - empty fields", () => {
    const result = validateRegister({
        name: "",
        username: "",
        email: "",
        password: "",
        repeatPassword: "",
    });

    expect(result).toEqual({
        name: "Name is required",
        username: "Username is required",
        email: "Email is required",
        password: "Password is required",
        repeatPassword: "Repeat password is required",
    });
});

test("register validation - valid input", () => {
    const result = validateRegister({
        name: "Ana",
        username: "ana123",
        email: "ana@email.com",
        password: "Strong1!",
        repeatPassword: "Strong1!",
    });

    expect(result).toEqual({});
});