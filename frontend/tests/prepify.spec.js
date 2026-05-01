import { test, expect } from '@playwright/test';

test.describe('Login', () => {
    test('user can login successfully', async ({ page }) => {
        await page.goto('http://localhost:5173/login');
        await page.fill('input[name="email"]', 'testuser');
        await page.fill('input[name="password"]', 'Password123!');
        await page.click('button:has-text("Login")');
        await expect(page).toHaveURL(/.*feed/);
    });

    test('login shows validation errors when empty', async ({ page }) => {
        await page.goto('http://localhost:5173/login');
        await page.click('button:has-text("Login")');

        await expect(page.getByText('Email is required')).toBeVisible();
        await expect(page.getByText('Password is required')).toBeVisible();
        await expect(page).toHaveURL(/.*login/);
    });
});

test.describe('Register', () => {
    test('user can register successfully', async ({ page }) => {
        await page.goto('http://localhost:5173/register');
        await page.fill('input[name="name"]', 'Test User');
        await page.fill('input[name="username"]', 'testuser');
        await page.fill('input[name="email"]', 'testuser@email.com');
        await page.fill('input[name="password"]', 'Password123!');
        await page.fill('input[name="repeatPassword"]', 'Password123!');
        await page.click('button:has-text("Register")');

        await expect(page).toHaveURL(/.*login/);
    });

    test('register shows validation errors when empty', async ({ page }) => {
        await page.goto('http://localhost:5173/register');
        await page.click('button:has-text("Register")');

        await expect(page.getByText('Name is required', { exact: true })).toBeVisible();
        await expect(page.getByText('Username is required', { exact: true })).toBeVisible();
        await expect(page.getByText('Email is required', { exact: true })).toBeVisible();
        await expect(page.getByText('Password is required', { exact: true })).toBeVisible();
        await expect(page.getByText('Repeat password is required', { exact: true })).toBeVisible();
    });

    test('register shows invalid email error', async ({ page }) => {
        await page.goto('http://localhost:5173/register');
        await page.fill('input[name="name"]', 'Test User');
        await page.fill('input[name="username"]', 'testuser');
        await page.fill('input[name="email"]', 'invalid-email');
        await page.fill('input[name="password"]', 'Password123!');
        await page.fill('input[name="repeatPassword"]', 'Password123!');
        await page.click('button:has-text("Register")');

        await expect(page.getByText('Invalid email format')).toBeVisible();
    });
});

test.describe('Add Recipe', () => {
    test('user can add a new recipe', async ({ page }) => {
        await page.goto('http://localhost:5173/add');
        await page.fill('input[name="name"]', 'Pasta');
        await page.fill('input[name="category"]', 'Dinner');
        await page.fill('input[name="servings"]', '2');
        await page.fill('input[name="time"]', '30 min');
        await page.fill('input[name="image"]', 'https://example.com/pasta.jpg');
        await page.fill('textarea[name="ingredients"]', 'Pasta\nSalt');
        await page.fill('textarea[name="steps"]', 'Boil\nServe');
        await page.fill('textarea[name="nutritionalValues"]', '200 kcal');
        await page.click('button:has-text("Add Recipe")');

        await expect(page).toHaveURL(/.*feed/);
    });

    test('shows errors when form is empty', async ({ page }) => {
        await page.goto('http://localhost:5173/add');
        await page.click('button:has-text("Add Recipe")');
        await expect(page.locator('.error').first()).toBeVisible();
    });
});

test.describe('Recipe Management', () => {
    test('user can view recipe details', async ({ page }) => {
        await page.goto('http://localhost:5173/feed');
        const firstRecipe = page.locator('tbody tr td:nth-child(2)').first();
        const recipeName = await firstRecipe.textContent();
        await firstRecipe.click();

        await expect(page).toHaveURL(/.*recipe/);
        await expect(page.locator('h2')).toContainText(recipeName);
    });

    test('user can delete a recipe', async ({ page }) => {
        await page.goto('http://localhost:5173/feed');
        const firstRecipe = page.locator('tbody tr').first();
        const recipeName = await firstRecipe.locator('td:nth-child(2)').textContent();

        page.on('dialog', dialog => dialog.accept());
        await firstRecipe.locator('text=Delete').click();

        await expect(page.locator(`text=${recipeName}`)).not.toBeVisible();
    });
});