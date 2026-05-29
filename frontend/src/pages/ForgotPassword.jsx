import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API_BASE from "../config.js";
import "./Login.css";

export default function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [sent, setSent] = useState(false);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleForgot = async () => {
        if (!email) return;
        try {
            const response = await fetch(`${API_BASE}/api/users/forgot-password`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });
            const data = await response.json();
            if (response.ok) {
                setSent(true);
            } else {
                setError(data.error || "Something went wrong");
            }
        } catch {
            setError("Error occurred. Please try again.");
        }
    };

    return (
        <div className="login-container">
            <h1 className="logo" style={{ marginLeft: "10px" }}>PREPIFY</h1>
            <div className="login-box">
                <h2>FORGOT PASSWORD</h2>
                {!sent ? (
                    <>
                        <p>enter your email</p>
                        <input
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                        />
                        {error && <p className="error">{error}</p>}
                        <button onClick={handleForgot}>Send Reset Link</button>
                    </>
                ) : (
                    <>
                        <p style={{ color: "#AF776F", marginBottom: "1rem" }}>
                            Check your email for a password reset link!
                        </p>
                        <p style={{ color: "#999", fontSize: "12px" }}>
                            The link expires in 30 minutes.
                        </p>
                    </>
                )}
                <p style={{ marginTop: "1rem" }}>
                    <a href="/login">Back to login</a>
                </p>
            </div>
        </div>
    );
}