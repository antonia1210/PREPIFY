import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import API_BASE from "../config.js";
import "./Login.css"; // reuse same styles

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState("");
    const [done, setDone] = useState(false);
    const navigate = useNavigate();
    const token = searchParams.get("token");

    const handleSubmit = async () => {
        if (!password || password.length < 6) {
            setError("Password must be at least 6 characters");
            return;
        }
        if (password !== confirm) {
            setError("Passwords do not match");
            return;
        }
        try {
            const res = await fetch(`${API_BASE}/api/users/reset-password`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ resetToken: token, newPassword: password })
            });
            const data = await res.json();
            if (data.error) {
                setError(data.error);
            } else {
                setDone(true);
                setTimeout(() => navigate("/login"), 2000);
            }
        } catch {
            setError("Something went wrong. Please try again.");
        }
    };

    if (!token) {
        return (
            <div className="login-container">
                <div className="login-box">
                    <h2>Invalid Link</h2>
                    <p>This reset link is invalid or has expired.</p>
                    <button onClick={() => navigate("/login")}>Back to Login</button>
                </div>
            </div>
        );
    }

    return (
        <div className="login-container">
            <h1 className="logo" style={{ marginLeft: "10px" }}>PREPIFY</h1>
            <div className="login-box">
                <h2>RESET PASSWORD</h2>
                {!done ? (
                    <>
                        <p>new password</p>
                        <input
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                        />
                        <p>confirm password</p>
                        <input
                            type="password"
                            value={confirm}
                            onChange={e => setConfirm(e.target.value)}
                        />
                        {error && <p className="error">{error}</p>}
                        <button onClick={handleSubmit}>Reset Password</button>
                    </>
                ) : (
                    <p style={{ color: "#AF776F" }}>Password reset! Redirecting to login...</p>
                )}
            </div>
        </div>
    );
}