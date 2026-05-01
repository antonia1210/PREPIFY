export function validateRegister(form) {
    const errors = {};

    if (!form.name) errors.name = "Name is required";
    if (!form.username) errors.username = "Username is required";
    if (!form.email) {
        errors.email = "Email is required";
    } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(form.email)) {
            errors.email = "Invalid email format";
        }
    }
    if (!form.password) {
        errors.password = "Password is required";
    } else {
        const strongPassword =
            form.password.length >= 8 &&
            /[A-Z]/.test(form.password) &&
            /\d/.test(form.password) &&
            /[^A-Za-z0-9]/.test(form.password);

        if (!strongPassword) {
            errors.password = "Min 8 characters, 1 uppercase, 1 number, 1 special character";
        }
    }

    if (!form.repeatPassword) {
        errors.repeatPassword = "Repeat password is required";
    } else if (form.password !== form.repeatPassword) {
        errors.repeatPassword = "Passwords do not match";
    }

    return errors;
}