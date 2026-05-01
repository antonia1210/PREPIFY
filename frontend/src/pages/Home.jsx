import { useNavigate } from "react-router-dom";
import "./Home.css";
import { useEffect } from "react";
import { setCookie, getCookie } from "../utils/cookies";

export default function Home() {
    const navigate = useNavigate();
    useEffect(() => {
        const visits = parseInt(getCookie("visits") || "0");
        setCookie("visits", visits + 1);
    }, []);
    const lastRecipe = getCookie("lastViewedRecipe");
    const visits = getCookie("visits");
    return (
        <div className="container">
            <div className="title">
                <h1>PREPIFY</h1>
            </div>
            <p className="tagline">
                "Cook smarter with what you already have.
                <br />
                All you need in one place.”
            </p>
            <p className="description">
                Prepify is a smart meal planning and recipe management application that
                helps users organize recipes, track pantry ingredients, and discover
                meals based on what they already have. The app allows users to plan
                weekly meals, manage dietary restrictions, and generate shopping lists
                automatically.
            </p>
            <p className="description">
                {lastRecipe
                    ? `Last viewed recipe: ${lastRecipe}`
                    : "No recipes viewed yet"}
            </p>
            {visits && (
                <p className="description">
                    Total visits: {visits}
                </p>
            )}
            <div className="buttons">
                <button className="primary" onClick={() => navigate("/register")}>
                    Get Started!</button>
                <button className="secondary" onClick={() => navigate("/login")}>
                    Login</button>
            </div>
        </div>
    )
}