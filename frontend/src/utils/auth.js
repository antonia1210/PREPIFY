export function getUser() {
    return JSON.parse(localStorage.getItem("user") || "null");
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
    return getUser() !== null;
}