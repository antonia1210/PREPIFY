import { useParams, useNavigate } from "react-router-dom";
import "./RecipeDetails.css"
import Navbar from "./Navbar";
import {useEffect} from "react";
import { useState } from "react";
import {getCookie, setCookie} from "../utils/cookies";
import { hasPermission, isAdmin, getUser } from "../utils/auth";
import API_BASE from "../config.js"

export default function RecipeDetails() {
    const [selectedRating, setSelectedRating] = useState(0);
    const [recipe, setRecipe] = useState(null);
    const { id } = useParams();
    const [loading, setLoading] = useState(true);

    const navigate = useNavigate();
    useEffect(() => {
        const fetchRecipe = async () => {
            try {
                const response = await fetch(`${API_BASE}/api/recipes/${id}`);
                if (!response.ok) {
                    throw new Error("Failed to fetch recipe");
                }
                const data = await response.json();
                setRecipe(data);
            } catch (error) {
                console.error("Error fetching recipe:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchRecipe();
    }, [id]);
    useEffect(() => {
        if (recipe) {
            setCookie("lastViewedRecipe", recipe.name, 1);
        }
    }, [recipe]);
    const handleDelete = async () => {
        const confirmDelete = window.confirm("Are you sure you want to delete this recipe?");
        if (!confirmDelete) return;
        try {
            const response = await fetch(`${API_BASE}/api/recipes/${recipe.id}?userId=${getUser()?.id}`, {
                method: "DELETE"});
            if (!response.ok) {
                throw new Error("Failed to delete recipe");
            }
            navigate("/feed");
        } catch (error) {
            console.error("Error deleting recipe:", error);
            alert("Could not delete recipe.");
        }
    };
    const handleRate = async (value) => {
        try {
            const response = await fetch(`${API_BASE}/api/recipes/${recipe.id}/rating`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ rating: value })
            });
            if (!response.ok) {
                throw new Error("Failed to rate recipe");
            }
            const text = await response.text();
            const updatedRecipe = JSON.parse(text);
            setSelectedRating(value);
            setRecipe(updatedRecipe);
        } catch (error) {
            console.error("Error rating recipe:", error);
            alert("Could not save rating.");
        }
    };
    if(loading) return <div>Loading...</div>;
    if(!recipe) return <div>Recipe not found</div>;
return (
    <>
        <Navbar />
        <div className="details-container">
            <div className="card">
            <div className="top-section">
            <h1 className="logo">PREPIFY</h1>
                <div className="top-actions">
            {(isAdmin() || getUser()?.id == recipe.userId) && (
                    <>
                    <button onClick={() => navigate(`/edit/${recipe.id}`)}>/Edit Recipe</button>
                    <button onClick={handleDelete}>-Delete Recipe</button>
                </>
                    )}
                </div>
            </div>
            <h2 className="title">{recipe.name}</h2>
            <p className="author-name">recipe by <strong>{recipe.authorName}</strong></p>
            <div className="content">
                <div className="left-content">
                    {recipe.image ? (
                        <img src={recipe.image} alt={recipe.name} />
                    ) : (
                        <div className="no-image">No image</div>
                    )}
                    <p> 🥗 Category: {recipe.category}</p>
                    <p>
                        🍽️ Servings: {recipe.servings}
                    </p>
                    <p>
                        ⏳ Preparation Time: {recipe.preparationTime}
                    </p>

                </div>

                <div className="right-content">
                    <h3>🧾 Ingredients:</h3>
                    <ul>
                        {recipe.ingredients.map((ingredient, index) => (
                            <li key={index}>
                                {ingredient.quantity} {ingredient.unit} {ingredient.name}
                            </li>
                        ))}
                    </ul>

                </div>
            </div>
            <div className="more">
                <h3>👨‍🍳 Steps:</h3>
                <ol>
                    {recipe.steps.map((step, index) => (
                        <li key={index}>{step}</li>
                    ))}
                </ol>
                <br></br>
                <h3>💪 Nutritional values:</h3>
                <div className="nutrition">
                    {recipe.nutritionalValues.map((value, index) => (
                        <div className="nutrition-card" key={index}>
                            {value.name}: {value.amount} {value.unit}
                        </div>
                    ))}
                </div>
                {getUser()?.id != recipe.userId && (
                    <>
                <h3>Rate this recipe
                    <div className="rating">

                        {[1, 2, 3, 4, 5].map(value => (
                            <span
                                key={value}
                                onClick={() => handleRate(value)}
                                style={{
                                    cursor: "pointer",
                                    fontSize: "30px",
                                    color: value <= selectedRating ? "gold" : "gray"
                                }}
                            >
                                {value <= selectedRating ? "★" : "☆"}
                            </span>
                        ))}


                        <div className="rating-average">
                            Average rating: {(recipe.averageRating ?? 0).toFixed(1)}
                        </div>
                    </div>
                </h3>
                    </>
                )}
            </div>
                <div className="bottom-actions">
                    <button className="back" onClick={() => navigate("/feed")}>Back to recipes</button>
                    {getUser()?.id !== recipe.userId && recipe.userId && (
                        <button className="chat-author-btn" onClick={() => navigate(`/chat/${recipe.userId}`)}>
                            💬 Chat with {recipe.authorName}
                        </button>
                    )}
                    <button className="add">Add to plan</button>
                </div>
            </div>
        </div>
        </>
    );
}