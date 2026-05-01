import "./Navbar.css";
import {useNavigate} from "react-router-dom";
import {setCookie} from "../utils/cookies";
import {useEffect, useState} from "react";

export default function Navbar() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    useEffect(() => {
        const stored = localStorage.getItem("user");
        if (!stored) {
            navigate("/login");
            return;
        }
        const parsedUser = JSON.parse(stored);
        setUser(parsedUser);
    }, []);

    if (!user) return null;

    return (
        <div className="navbar">
            <div className="nav-links">
                <button onClick={() => {setCookie('lastPage','/feed'); navigate("/feed")}}>Recipes</button>
                <button>My Planner</button>
                <button>My Fridge</button>
                <button>Shopping List</button>
                <button onClick={() => navigate("/statistics")}>Statistics</button>
            </div>
            <div className="profile-circle" onClick={() => navigate("/user-profile")}>
                {user.name?.charAt(0).toUpperCase()}
            </div>
        </div>
    );
}