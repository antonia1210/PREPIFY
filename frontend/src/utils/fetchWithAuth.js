import API_BASE from "../config.js";
import {  getRefreshToken, authHeaders } from "./auth.js";

export async function fetchWithAuth(url, options = {}) {
    let response = await fetch(url, {
        ...options,
        headers: { ...authHeaders(), ...options.headers }
    });
    if (response.status === 401) {
        const refreshToken = getRefreshToken();
        if (!refreshToken) {
            redirectToLogin();
            return response;
        }

        const refreshResponse = await fetch(`${API_BASE}/api/users/refresh`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken })
        });

        if (refreshResponse.ok) {
            const data = await refreshResponse.json();
            localStorage.setItem("token", data.token);
            response = await fetch(url, {
                ...options,
                headers: {
                    ...options.headers,
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${data.token}`
                }
            });
        } else {
            redirectToLogin();
        }
    }

    return response;
}

function redirectToLogin() {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    window.location.href = "/login";
}