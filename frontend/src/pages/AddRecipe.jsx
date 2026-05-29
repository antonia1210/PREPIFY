import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import { useParams } from "react-router-dom";
import "./AddRecipe.css"
import {validateRecipe} from "../validation/recipeValidation.js";
import {setCookie} from "../utils/cookies";
import { useOnlineStatus } from "../utils/useOnlineStatus";
import { useOfflineQueue } from "../utils/useOfflineQueue";
import { getUser } from "../utils/auth";
import API_BASE from "../config.js"
import { authHeaders } from "../utils/auth";

export default function AddRecipe() {
    const isOnline = useOnlineStatus();
    const { enqueue } = useOfflineQueue();
    const navigate = useNavigate();
    const {id} = useParams();
    const [error, setError] = useState({});
    const [form, setForm] = useState({
                name: "",
                category: "",
                servings: "",
                preparationTime: "",
                image: "",
    });
    const [ingredients, setIngredients] = useState([
        { name: "", quantity: "", unit: "" }
    ]);

    const [nutritionalValues, setNutritionalValues] = useState([
        { name: "", amount: "", unit: "" }
    ]);

    const [steps, setSteps] = useState([""]);

    const handleIngredientChange = (index, field, value) => {
        const updated = [...ingredients];
        updated[index][field] = value;
        setIngredients(updated);
    };

    const addIngredient = () => {
        setIngredients([...ingredients, { name: "", quantity: "", unit: "" }]);
    };

    const removeIngredient = (index) => {
        setIngredients(ingredients.filter((_, i) => i !== index));
    };

    const handleNutritionalChange = (index, field, value) => {
        const updated = [...nutritionalValues];
        updated[index][field] = value;
        setNutritionalValues(updated);
    }

    const addNutritionalValue = () => {
        setNutritionalValues([...nutritionalValues, { name: "", amount: "", unit: "" }]);
    }

    const removeNutritionalValue = (index) => {
        setNutritionalValues(nutritionalValues.filter((_, i) => i !== index));
    }

    const handleStepChange = (index, value) => {
        const updated = [...steps];
        updated[index] = value;
        setSteps(updated);
    };

    const addStep = () => {
        setSteps([...steps, ""]);
    };

    const removeStep = (index) => {
        setSteps(steps.filter((_, i) => i !== index));
    };

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value,
        });
    }
    useEffect(() => {
        if (!id) return;
        const fetchRecipe = async () => {
            try {
                const response = await fetch(`${API_BASE}/api/recipes/${id}`, {
                    headers: authHeaders(),
                });
                if (!response.ok) throw new Error("Failed to fetch recipe");
                const data = await response.json();
                localStorage.setItem(`recipe_${id}`, JSON.stringify(data));
                populateForm(data);
            } catch (error) {
                console.error("Error fetching recipe:", error);
                const cached = localStorage.getItem(`recipe_${id}`);
                if (cached) populateForm(JSON.parse(cached));
            }
        };

        const populateForm = (data) => {
            setForm({
                name: data.name || "",
                category: data.category || "",
                servings: Number(data.servings) || "",
                preparationTime: Number(data.preparationTime) || "",
                image: data.image || "",
            });
            if (data.ingredients && data.ingredients.length > 0) {
                setIngredients(data.ingredients.map(i => ({
                    name: i.name || "",
                    quantity: i.quantity || "",
                    unit: i.unit || ""
                })));
            }
            if (data.steps && data.steps.length > 0) {
                setSteps(data.steps);
            }
            if (data.nutritionalValues && data.nutritionalValues.length > 0) {
                setNutritionalValues(data.nutritionalValues.map(n => ({
                    name: n.name || "",
                    amount: n.amount || "",
                    unit: n.unit || ""
                })));
            }
        };

        fetchRecipe();
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const validationErrors = validateRecipe(form);
        if (Object.keys(validationErrors).length > 0) {
            setError(validationErrors);
            return;
        }
        const recipeData = {
            name: form.name,
            category: form.category,
            servings: form.servings,
            preparationTime: form.preparationTime,
            image: form.image,
            ingredients: ingredients.filter(i => i.name).map(i => ({
                name: i.name,
                quantity: parseFloat(i.quantity),
                unit: i.unit
            })),
            steps: steps.filter(s => s.trim()).map(s => s.trim()),
            nutritionalValues: nutritionalValues.filter(n => n.name).map(n => ({
                name: n.name,
                amount: parseFloat(n.amount),
                unit: n.unit
            })),
        };
        if (!isOnline) {
            if (id) {
                enqueue({ type: "UPDATE", id, payload: recipeData });
                localStorage.setItem(`recipe_${id}`, JSON.stringify(recipeData));
            } else {
                const tempRecipe = { ...recipeData, id: `temp_${Date.now()}` };
                const cached = JSON.parse(localStorage.getItem("localRecipes") || "[]");
                cached.push(tempRecipe);
                localStorage.setItem("localRecipes", JSON.stringify(cached));
                enqueue({ type: "CREATE", payload: recipeData });
            }
            navigate("/feed");
            return;
        }
        try {
            let response;
            if (id) {
                response = await fetch(`${API_BASE}/api/recipes/${id}?userId=${getUser()?.id}`, {
                    method: "PUT",
                    headers: authHeaders(),
                    body: JSON.stringify(recipeData),
                });
            } else {
                response = await fetch(`${API_BASE}/api/recipes?userId=${getUser()?.id}`, {
                    method: "POST",
                    headers: authHeaders(),
                    body: JSON.stringify(recipeData),
                });
            }
            if (!response.ok) {
                throw new Error("Failed to add recipe");
            }
            setCookie("favoriteCategory", form.category);
            navigate("/feed");
        }catch (error) {
            console.error("Error adding recipe:", error);
        }
    };

    return(
        <div className="add-recipe-container">
            <h1 className="logo">PREPIFY</h1>
            <div className="fields">
                <form onSubmit={handleSubmit} className="form-card">
                    <h2 className="form-title">
                        {id ? "Edit Recipe" : "Add New Recipe"}
                    </h2>

                    <div className="form-grid">
                        <div className="left-column">
                            <label>Name</label>
                            <input name="name" value={form.name} onChange={handleChange} />
                            {error.name && <span className="error">{error.name}</span>}

                            <label>Category</label>
                            <input name="category" value={form.category} onChange={handleChange} />
                            {error.category && <span className="error">{error.category}</span>}

                            <label>Servings</label>
                            <input type="number" name="servings" value={form.servings} onChange={handleChange} />
                            {error.servings && <span className="error">{error.servings}</span>}

                            <label>Preparation Time</label>
                            <input type="number" name="preparationTime" value={form.preparationTime} onChange={handleChange} />
                            {error.preparationTime && <span className="error">{error.preparationTime}</span>}

                            <label>Image URL</label>
                            <input name="image" value={form.image} onChange={handleChange} />
                        </div>


                        <div className="right-column">
                            <label>Ingredients</label>
                            {ingredients.map((ing, index) => (
                                <div key={index} className="ingredient-row">
                                    <input
                                        placeholder="name"
                                        value={ing.name}
                                        onChange={(e) => handleIngredientChange(index, "name", e.target.value)}
                                    />
                                    <input
                                        type="number"
                                        placeholder="quantity"
                                        value={ing.quantity}
                                        onChange={(e) => handleIngredientChange(index, "quantity", e.target.value)}
                                    />
                                    <input
                                        placeholder="unit"
                                        value={ing.unit}
                                        onChange={(e) => handleIngredientChange(index, "unit", e.target.value)}
                                    />
                                    <button type="button" onClick={() => removeIngredient(index)}>✕</button>
                                </div>
                            ))}
                            <button type="button" className="add-row-btn" onClick={addIngredient}>+ Add Ingredient</button>

                            <label>Steps</label>
                            {steps.map((step, index) => (
                                <div key={index} className="ingredient-row">
                                    <input
                                        placeholder={`Step ${index + 1}`}
                                        value={step}
                                        onChange={(e) => handleStepChange(index, e.target.value)}
                                    />
                                    <button type="button" onClick={() => removeStep(index)}>✕</button>
                                </div>
                            ))}
                            <button type="button" className="add-row-btn" onClick={addStep}>+ Add Step</button>

                            <label>Nutritional Values</label>
                            {nutritionalValues.map((nutr, index) => (
                                <div key={index} className="ingredient-row">
                                    <input
                                        placeholder="name"
                                        value={nutr.name}
                                        onChange={(e) => handleNutritionalChange(index, "name", e.target.value)}
                                    />
                                    <input
                                        type="number"
                                        placeholder="amount"
                                        value={nutr.amount}
                                        onChange={(e) => handleNutritionalChange(index, "amount", e.target.value)}
                                    />
                                    <input
                                        placeholder="unit"
                                        value={nutr.unit}
                                        onChange={(e) => handleNutritionalChange(index, "unit", e.target.value)}
                                    />
                                    <button type="button" onClick={() => removeNutritionalValue(index)}>✕</button>
                                </div>
                            ))}
                            <button type="button" className="add-row-btn" onClick={addNutritionalValue}>+ Add Nutritional Value</button>
                        </div>
                    </div>
                    {form.image && (
                        <div className="image-preview">
                            <img src={form.image} alt="preview" />
                        </div>
                    )}
                    <button type="submit" className="submit-btn">
                        {id ? "Update Recipe" : "Add Recipe"}
                    </button>

                </form>
            </div>
        </div>
    )
}