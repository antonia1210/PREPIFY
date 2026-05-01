export function validateLogin(form) {
    const errors = {};

    if (!form.email) errors.email = "Email is required";
    if (!form.password) errors.password = "Password is required";

    return errors;
}