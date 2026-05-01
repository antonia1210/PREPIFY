export function validateRecipe(form) {
    const errors = {};

    if (!form.name) errors.name = "Name cannot be empty field";
    if (!form.category) errors.category = "Category cannot be empty field";

    if (form.servings === "") {
        errors.servings = "Servings cannot be empty field";
    } else if (Number(form.servings) < 0) {
        errors.servings = "Servings cannot be negative";
    }
    if(Number(form.preparationTime) < 0){
        errors.preparationTime = "Preparation time cannot be negative";
    }

    return errors;
}