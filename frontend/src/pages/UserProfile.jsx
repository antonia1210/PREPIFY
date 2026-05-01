import Navbar from "./Navbar";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./UserProfile.css";

export default function Profile() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [recipes, setRecipes] = useState([]);

    useEffect(() => {
        const stored = localStorage.getItem("user");
        if (!stored) {
            navigate("/login");
            return;
        }
        const parsedUser = JSON.parse(stored);
        setUser(parsedUser);

        fetch(`http://localhost:8080/api/recipes/user/${parsedUser.id}`)
            .then(res => res.json())
            .then(data => setRecipes(data));
    }, []);

    if (!user) return null;

    return (
        <>
            <Navbar />
            <div className="profile-page">
                <h1 className="logo">PREPIFY</h1>
                <div className="profile-container">
                    <div className="profile-card">
                        <div className="profile-avatar">
                            {user.name?.charAt(0).toUpperCase()}
                        </div>
                        <h2 className="profile-name">{user.name}</h2>
                        <span className="profile-preference">{user.preferences}</span>
                        <div className="profile-details">
                            <div className="profile-field">
                                <span className="field-label">Username</span>
                                <span className="field-value">{user.username}</span>
                            </div>
                            <div className="profile-field">
                                <span className="field-label">Email</span>
                                <span className="field-value">{user.email}</span>
                            </div>
                            <div className="profile-field">
                                <span className="field-label">Dietary Preference</span>
                                <span className="field-value">{user.preferences || "None"}</span>
                            </div>
                            <div className="profile-field">
                                <span className="field-label">Recipes</span>
                                <span className="field-value">{recipes.length}</span>
                            </div>
                        </div>
                    </div>

                    <div className="profile-recipes">
                        <h3>My Recipes</h3>
                        {recipes.length === 0 ? (
                            <p className="no-recipes">No recipes yet. <span onClick={() => navigate("/add")} style={{cursor: "pointer", textDecoration: "underline"}}>Add one!</span></p>
                        ) : (
                            <div className="recipe-grid">
                                {recipes.map(recipe => (
                                    <div key={recipe.id} className="recipe-card" onClick={() => navigate(`/recipe/${recipe.id}`)}>
                                        <img src={recipe.image} alt={recipe.name} className="recipe-card-img" />
                                        <div className="recipe-card-info">
                                            <h4>{recipe.name}</h4>
                                            <span className="recipe-card-category">{recipe.category}</span>
                                            <span className="recipe-card-servings">🍽 {recipe.servings} servings</span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}