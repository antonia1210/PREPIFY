import API_BASE from "../config.js"

export function getUser() {
    return JSON.parse(localStorage.getItem("user") || "null");
}

export function getToken() {
    return localStorage.getItem("token");
}

export function hasRole(role) {
    const user = getUser();
    return user?.roles?.includes(role);
}

export function hasPermission(permission) {
    const user = getUser();
    return user?.permissions?.includes(permission);
}

export function isAdmin() {
    return hasRole("ADMIN");
}

export function isLoggedIn() {
    return getUser() !== null && getToken() !== null;
}

export function authHeaders() {
    const token = getToken();
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
    };
}

export function getRefreshToken() {
    return localStorage.getItem("refreshToken");
}

export async function logout() {
    const token = getToken();
    if (token) {
        await fetch(`${API_BASE}/api/users/logout`, {
            method: "POST",
            headers: authHeaders()
        }).catch(() => {});
    }
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
}