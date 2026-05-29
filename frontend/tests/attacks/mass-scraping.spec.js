import { test } from '@playwright/test';

const BASE_URL = 'https://192.168.1.148:8443';

test('mass scraping attack', async ({ request }) => {
    console.log('Starting scraping attack...');

    // Get all recipes then hit each one
    const res = await request.get(`${BASE_URL}/api/recipes?page=0&size=50`, {
        ignoreHTTPSErrors: true
    });
    const recipes = await res.json();

    for (const recipe of recipes) {
        await request.get(`${BASE_URL}/api/recipes/${recipe.id}`, {
            ignoreHTTPSErrors: true
        });
        console.log(`Scraped recipe: ${recipe.id}`);
    }
    console.log('Scraping attack complete');
});