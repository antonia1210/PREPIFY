import { test } from '@playwright/test';

const BASE_URL = 'https://192.168.1.148:8443';

test('brute force login attack', async ({ request }) => {
    console.log('Starting brute force attack...');

    // Try 20 wrong passwords rapidly
    for (let i = 0; i < 20; i++) {
        const response = await request.post(`${BASE_URL}/api/users/login`, {
            data: {
                email: 'szalokantonia72@gmail.com',
                password: `wrongpassword${i}`
            },
            ignoreHTTPSErrors: true
        });
        console.log(`Attempt ${i + 1}: ${response.status()}`);
    }
    console.log('Brute force attack complete');
});