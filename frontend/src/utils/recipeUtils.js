export function addRecipe(recipes, recipe) {
    return [...recipes, { ...recipe, id: Date.now() }];
}

export function updateRecipe(recipes, updatedRecipe) {
    return recipes.map(r =>
        r.id === updatedRecipe.id ? updatedRecipe : r
    );
}

export function deleteRecipe(recipes, id) {
    return recipes.filter(r => r.id !== id);
}