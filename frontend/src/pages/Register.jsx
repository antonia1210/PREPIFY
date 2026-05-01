import {useState} from "react";
import "./Register.css"
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import {validateRegister} from "../validation/registerValidation.js";
export default function Register() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        name:"",
        email: "",
        username: "",
        password: "",
        repeatPassword: "",
        preferences: "",
    });

    const [error, setError] = useState({});
    const handleChange = (e) => {
    setForm({
        ...form,
        [e.target.name]: e.target.value,
    });
    };

    const handleSubmit = async () => {
        const validationErrors = validateRegister(form);
        if (Object.keys(validationErrors).length > 0) {
            setError(validationErrors);
            return;
        }
        try {
            const response = await fetch("http://localhost:8080/api/users/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: form.name,
                    email: form.email,
                    username: form.username,
                    password: form.password,
                    preferences: form.preferences
                })
            });
            if (!response.ok) {
                const msg = await response.text();
                setError({ username: msg });
                return;
            }
            navigate("/login");
        } catch (error) {
            console.error("Register error:", error);
        }
    };

    return (
        <div className="register-container">
            <Link to="/" className="back-link">
            <h1 className="logo">PREPIFY</h1>
            </Link>
            <div className="register-box">
                <h2>REGISTER</h2>
                <label>name</label>
                <input type="text" name="name" value={form.name} onChange={handleChange}/>
                {error.name && <p className="error">{error.name}</p>}
                <label>email</label>
                <input type="text" name="email" value={form.email} onChange={handleChange}/>
                {error.email && <p className="error">{error.email}</p>}
                <label>username</label>
                <input type="text" name="username" value={form.username} onChange={handleChange}/>
                {error.username && <p className="error">{error.username}</p>}
                <label>password</label>
                <input type="password" name="password" value={form.password} onChange={handleChange}/>
                <p className="hint">
                    min. 8 characters, an upper letter, a digit and a symbol
                </p>
                {error.password && <p className="error">{error.password}</p>}
                <label>repeat password</label>
                <input type="password" name="repeatPassword" value={form.repeatPassword} onChange={handleChange}/>
                {error.repeatPassword && <p className="error">{error.repeatPassword}</p>}
                <label>dietary preferences</label>
                <br></br>
                <select
                    name="preferences"
                    value={form.preferences}
                    onChange={handleChange}
                >
                    <option value="">Select preference</option>
                    <option value="vegan">Vegan</option>
                    <option value="vegetarian">Vegetarian</option>
                    <option value="lactose">Lactose intolerant</option>
                    <option value="gluten">Gluten-free</option>
                    <option value="kosher">Kosher</option>
                    <option value="halal">Halal</option>
                    <option value="other">Other</option>
                </select>
                {error.preferences && (
                    <p className="error">{error.preferences}</p>
                )}
                <button onClick={handleSubmit}>Register</button>
            </div>
            <p className="login-link">Already have an account? <a href="/login">Login</a></p>
        </div>
    );
}