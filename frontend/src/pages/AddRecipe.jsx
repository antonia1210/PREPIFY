import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import { useParams } from "react-router-dom";
import "./AddRecipe.css"
import {validateRecipe} from "../validation/recipeValidation.js";
import {setCookie} from "../utils/cookies";
import { useOnlineStatus } from "../utils/useOnlineStatus";
import { useOfflineQueue } from "../utils/useOfflineQueue";
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
                ingredients: "",
                steps: "",
                nutritionalValues: "",
    });

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
                const response = await fetch(`http://localhost:8080/api/recipes/${id}`);
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
                ingredients: Array.isArray(data.ingredients) ? data.ingredients.join("\n") : "",
                steps: Array.isArray(data.steps) ? data.steps.join("\n") : "",
                nutritionalValues: Array.isArray(data.nutritionalValues) ? data.nutritionalValues.join("\n") : "",
            });
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
            ingredients: form.ingredients
                .split("\n")
                .map(item => item.trim())
                .filter(item => item !== ""),
            steps: form.steps
                .split("\n")
                .map(item => item.trim())
                .filter(item => item !== ""),
            nutritionalValues: form.nutritionalValues
                .split("\n")
                .map(item => item.trim())
                .filter(item => item !== ""),
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
                response = await fetch(`http://localhost:8080/api/recipes/${id}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(recipeData),
                });
            } else {
                response = await fetch("http://localhost:8080/api/recipes", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(recipeData),
                })
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
                            <textarea name="ingredients" value={form.ingredients} onChange={handleChange} />

                            <label>Steps</label>
                            <textarea name="steps" value={form.steps} onChange={handleChange} />

                            <label>Nutritional Values</label>
                            <textarea name="nutritionalValues" value={form.nutritionalValues} onChange={handleChange} />
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