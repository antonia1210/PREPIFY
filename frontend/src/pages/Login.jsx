import "./Login.css"
import { Link, useNavigate } from "react-router-dom";
import { validateLogin } from "../validation/loginValidation.js";
import API_BASE from "../config.js"
import { useState, useRef } from "react";

export default function Login() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ email: "", password: "" });
    const [error, setError] = useState({});
    const [step, setStep] = useState(1); // 1 = password, 2 = OTP, 3 = security question
    const [otpEmail, setOtpEmail] = useState("");
    const [otpError, setOtpError] = useState("");
    const [securityQuestion, setSecurityQuestion] = useState("");
    const [securityAnswer, setSecurityAnswer] = useState("");
    const [securityError, setSecurityError] = useState("");

    const otpRefs = [useRef(), useRef(), useRef(), useRef(), useRef(), useRef()];
    const [otpDigits, setOtpDigits] = useState(["", "", "", "", "", ""]);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleOtpChange = (index, value) => {
        if (!/^\d*$/.test(value)) return;
        const newDigits = [...otpDigits];
        newDigits[index] = value.slice(-1);
        setOtpDigits(newDigits);
        if (value && index < 5) otpRefs[index + 1].current.focus();
    };

    const handleOtpKeyDown = (index, e) => {
        if (e.key === "Backspace" && !otpDigits[index] && index > 0) {
            otpRefs[index - 1].current.focus();
        }
    };

    const handleOtpPaste = (e) => {
        e.preventDefault();
        const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
        const newDigits = [...otpDigits];
        pasted.split("").forEach((char, i) => { newDigits[i] = char; });
        setOtpDigits(newDigits);
        const nextEmpty = Math.min(pasted.length, 5);
        otpRefs[nextEmpty].current.focus();
    };

    // Step 1: email + password
    const handleSubmit = async () => {
        const validationErrors = validateLogin(form);
        if (Object.keys(validationErrors).length > 0) {
            setError(validationErrors);
            return;
        }
        try {
            const response = await fetch(`${API_BASE}/api/users/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form)
            });
            const data = await response.json();
            if (!response.ok) {
                setError({ password: data.error || "Invalid credentials" });
                return;
            }
            // Admin skips OTP
            if (data.token) {
                localStorage.setItem("token", data.token);
                localStorage.setItem("refreshToken", data.refreshToken);
                localStorage.setItem("user", JSON.stringify(data.user));
                navigate("/feed");
                return;
            }
            setOtpEmail(data.email);
            setStep(2);
        } catch (error) {
            console.error("Login error:", error);
        }
    };

    // Step 2: OTP code
    const handleVerifyOtp = async () => {
        const code = otpDigits.join("");
        if (code.length < 6) return;
        try {
            const response = await fetch(`${API_BASE}/api/users/login/verify-otp`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: otpEmail, code })
            });
            const data = await response.json();
            if (!response.ok) {
                setOtpError(data.error || "Invalid code");
                setOtpDigits(["", "", "", "", "", ""]);
                otpRefs[0].current.focus();
                return;
            }
            // Fetch security question for step 3
            const userRes = await fetch(
                `${API_BASE}/api/users/security-question?email=${otpEmail}`
            );
            const userData = await userRes.json();
            setSecurityQuestion(userData.question || "");
            setStep(3);
        } catch (error) {
            console.error("OTP error:", error);
        }
    };

    // Step 3: security question
    const handleVerifySecurity = async () => {
        if (!securityAnswer) return;
        try {
            const response = await fetch(`${API_BASE}/api/users/login/verify-security`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: otpEmail, answer: securityAnswer })
            });
            const data = await response.json();
            if (!response.ok) {
                setSecurityError(data.error || "Incorrect answer");
                return;
            }
            localStorage.setItem("token", data.token);
            localStorage.setItem("refreshToken", data.refreshToken);
            localStorage.setItem("user", JSON.stringify(data.user));
            navigate("/feed");
        } catch (error) {
            console.error("Security error:", error);
        }
    };

    return (
        <div className="login-container">
            <Link to="/" className="back-link">
                <h1 className="logo">PREPIFY</h1>
            </Link>
            <div className={`login-box ${step === 2 ? "login-box-otp" : ""}`}>
                {step === 1 ? (
                    <>
                        <h2>LOGIN</h2>
                        <p>email</p>
                        <input type="email" name="email" value={form.email} onChange={handleChange} />
                        {error.email && <p className="error">{error.email}</p>}
                        <p>password</p>
                        <input type="password" name="password" value={form.password} onChange={handleChange} />
                        {error.password && <p className="error">{error.password}</p>}
                        <button onClick={handleSubmit}>Login</button>
                    </>
                ) : step === 2 ? (
                    <>
                        <h2>VERIFY</h2>
                        <p>A 6-digit code was sent to</p>
                        <p><strong>{otpEmail}</strong></p>
                        <div className="otp-boxes">
                            {otpDigits.map((digit, index) => (
                                <input
                                    key={index}
                                    ref={otpRefs[index]}
                                    type="text"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    onChange={e => handleOtpChange(index, e.target.value)}
                                    onKeyDown={e => handleOtpKeyDown(index, e)}
                                    onPaste={index === 0 ? handleOtpPaste : undefined}
                                    className="otp-box"
                                />
                            ))}
                        </div>
                        {otpError && <p className="error">{otpError}</p>}
                        <button onClick={handleVerifyOtp}>Verify</button>
                    </>
                ) : (
                    <>
                        <h2>SECURITY</h2>
                        <p style={{ marginBottom: "0.5rem" }}>Answer your security question</p>
                        <p><strong>{securityQuestion}</strong></p>
                        <input
                            type="text"
                            placeholder="Your answer"
                            value={securityAnswer}
                            onChange={e => setSecurityAnswer(e.target.value)}
                            onKeyDown={e => e.key === "Enter" && handleVerifySecurity()}
                        />
                        {securityError && <p className="error">{securityError}</p>}
                        <button onClick={handleVerifySecurity}>Verify</button>
                    </>
                )}
            </div>
            {step === 1 && (
                <>
                    <p className="forgot-password" onClick={() => navigate("/forgot-password")}>
                        forgot password?
                    </p>
                    <p className="register">Don't have an account? <a href="/register">Register</a></p>
                </>
            )}
        </div>
    );
}