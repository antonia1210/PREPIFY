import {useState} from "react";
import "./Login.css"
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import {validateLogin} from "../validation/loginValidation.js";
export default function Login() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        email: "",
        password: "",
    });
    const [error, setError] = useState({});
    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value,
        });
    };
    const handleSubmit = async () => {
        const validationErrors = validateLogin(form);
        if (Object.keys(validationErrors).length > 0) {
            setError(validationErrors);
            return;
        }
        try {
            const response = await fetch("http://localhost:8080/api/users/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form)
            });
            if (!response.ok) {
                setError({ password: "Invalid username or password" });
                return;
            }
            const user = await response.json();
            localStorage.setItem("user", JSON.stringify(user));
            navigate("/feed");
        } catch (error) {
            console.error("Login error:", error);
        }
    };

    return (
        <div className="login-container">
            <Link to="/" className="back-link">
                <h1 className="logo">PREPIFY</h1>
            </Link>
            <div className="login-box">
                <h2>LOGIN</h2>
                <p>email</p>
                <input type="email" name="email" value={form.email} onChange={handleChange} />
                {error.email && <p className="error">{error.email}</p>}
                <p>password</p>
                <input type="password" name="password" value={form.password} onChange={handleChange} />
                {error.password && <p className="error">{error.password}</p>}
                <button onClick={handleSubmit}>Login</button>
            </div>
            <p className="forgot-password">forgot password?</p>
            <p className="register">Don't have an account? <a href="/register">Register</a></p>
        </div>
    );
}