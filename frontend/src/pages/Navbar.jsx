import "./Navbar.css";
import {useNavigate} from "react-router-dom";
import {setCookie} from "../utils/cookies";
import {useEffect, useState} from "react";
import {isAdmin} from "../utils/auth";
import {useInactivityLogout} from "../utils/useInactivityLogout.js";

export default function Navbar() {
    useInactivityLogout();
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
            <div className="nav-right">
                <span className="chat-icon" onClick={() => navigate("/chat")}>💬</span>
                <div className="profile-circle" onClick={() => navigate("/user-profile")}>
                    {user.name?.charAt(0).toUpperCase()}
                </div>
            </div>
        </div>
    );
}