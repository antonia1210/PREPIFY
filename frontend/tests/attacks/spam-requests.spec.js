import { test } from '@playwright/test';

const BASE_URL = 'https://192.168.1.148:8443';

test('spam requests attack', async ({ request }) => {
    console.log('Starting spam attack...');

    // Fire 50 rapid requests to the recipes endpoint
    const promises = [];
    for (let i = 0; i < 50; i++) {
        promises.push(
            request.get(`${BASE_URL}/api/recipes?page=0&size=10`, {
                ignoreHTTPSErrors: true
            }).then(r => console.log(`Request ${i + 1}: ${r.status()}`))
        );
    }
    await Promise.all(promises);
    console.log('Spam attack complete');
});