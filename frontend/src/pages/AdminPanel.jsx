import { useEffect, useState } from "react";
import Navbar from "./Navbar";
import { isAdmin, authHeaders } from "../utils/auth";
import { useNavigate } from "react-router-dom";
import API_BASE from "../config.js";
import "./AdminPanel.css";

export default function AdminPanel() {
    const [suspicious, setSuspicious] = useState([]);
    const [seeding, setSeeding] = useState(false);
    const [seedResult, setSeedResult] = useState(null);
    const [seedUsers, setSeedUsers] = useState(500);
    const [seedRecipes, setSeedRecipes] = useState(2000);
    const [aiAnalysis, setAiAnalysis] = useState(null);
    const [analyzing, setAnalyzing] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAdmin()) {
            navigate("/feed");
            return;
        }
        fetch(`${API_BASE}/api/admin/suspicious`, { headers: authHeaders() })
            .then(res => res.json())
            .then(data => setSuspicious(data));
    }, []);

    const handleSeed = async () => {
        setSeeding(true);
        setSeedResult(null);
        try {
            const res = await fetch(`${API_BASE}/api/admin/seed`, {
                method: "POST",
                headers: { ...authHeaders(), "Content-Type": "application/json" },
                body: JSON.stringify({ users: seedUsers, recipes: seedRecipes })
            });
            const data = await res.json();
            setSeedResult(data);
        } catch {
            setSeedResult({ error: "Seeding failed" });
        } finally {
            setSeeding(false);
        }
    };

    const handleAiMonitor = async () => {
        setAnalyzing(true);
        try {
            const res = await fetch(`${API_BASE}/api/admin/ai-monitor`, {
                headers: authHeaders()
            });
            const data = await res.json();
            setAiAnalysis(data);
        } catch {
            setAiAnalysis({ error: "Analysis failed" });
        } finally {
            setAnalyzing(false);
        }
    };

    return (
        <>
            <div style={{ background: "#F5EFE6", minHeight: "100vh" }}>
                <Navbar />
                <div className="admin-container">

                    {/* Database Seeder */}
                    <h2 className="admin-title">Database Seeder</h2>
                    <div className="seed-box">
                        <div className="seed-inputs">
                            <label>
                                Users
                                <input
                                    type="number"
                                    value={seedUsers}
                                    onChange={e => setSeedUsers(Number(e.target.value))}
                                    min={1}
                                />
                            </label>
                            <label>
                                Recipes
                                <input
                                    type="number"
                                    value={seedRecipes}
                                    onChange={e => setSeedRecipes(Number(e.target.value))}
                                    min={1}
                                />
                            </label>
                            <button
                                className="seed-button"
                                onClick={handleSeed}
                                disabled={seeding}
                            >
                                {seeding ? "Seeding... (this may take a minute)" : "Seed Database"}
                            </button>
                        </div>
                        {seedResult && !seedResult.error && (
                            <p className="seed-success">
                                 Seeded {seedResult.users} users, {seedResult.recipes} recipes, {seedResult.ratings} ratings
                            </p>
                        )}
                        {seedResult?.error && (
                            <p className="seed-error"> {seedResult.error}</p>
                        )}
                    </div>

                    {/* AI Monitor */}
                    <h2 className="admin-title">AI Behavior Monitor</h2>
                    <div className="seed-box">
                        <button
                            className="seed-button"
                            onClick={handleAiMonitor}
                            disabled={analyzing}
                        >
                            {analyzing ? "Analyzing... (asking AI)" : " Analyze Suspicious Behavior"}
                        </button>

                        {aiAnalysis && !aiAnalysis.error && (
                            <div className="ai-results">
                                <div className="ai-stats">
                                    <span>Logs analyzed: <strong>{aiAnalysis.logsAnalyzed}</strong></span>
                                    <span>Unique IPs: <strong>{aiAnalysis.uniqueIps}</strong></span>
                                    <span>Window: <strong>{aiAnalysis.timeWindow}</strong></span>
                                    <span className={aiAnalysis.suspicious ? "ai-suspicious" : "ai-safe"}>
                                        {aiAnalysis.suspicious ? " SUSPICIOUS ACTIVITY DETECTED" : " No suspicious activity"}
                                    </span>
                                </div>
                                <div className="ai-analysis">
                                    <h4>AI Analysis:</h4>
                                    <p>{aiAnalysis.analysis}</p>
                                </div>
                                {aiAnalysis.logSummary && aiAnalysis.logSummary.length > 0 && (
                                    <div className="ai-log-summary">
                                        <h4>Activity by IP:</h4>
                                        {aiAnalysis.logSummary.map((item, i) => (
                                            <div key={i} className="ip-row">
                                                <span>{item.ip}</span>
                                                <span>{item.requestCount} requests</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}
                        {aiAnalysis?.error && <p className="seed-error"> {aiAnalysis.error}</p>}
                    </div>

                    {/* Suspicious Users */}
                    <h2 className="admin-title">Suspicious Users</h2>
                    {suspicious.length === 0 ? (
                        <p className="no-suspicious">No suspicious activity detected.</p>
                    ) : (
                        <table className="suspicious-table">
                            <thead>
                            <tr>
                                <th>User</th>
                                <th>IP</th>
                                <th>Reason</th>
                                <th>Violations</th>
                                <th>First Detected</th>
                                <th>Last Detected</th>
                            </tr>
                            </thead>
                            <tbody>
                            {suspicious.map((user, index) => (
                                <tr key={index} className={index % 2 === 0 ? "row-even" : "row-odd"}>
                                    <td>{user.username}</td>
                                    <td>{user.email}</td>
                                    <td className="reason">{user.reason}</td>
                                    <td className="violations">{user.violationCount}</td>
                                    <td>{new Date(user.firstDetected).toLocaleString()}</td>
                                    <td>{new Date(user.lastDetected).toLocaleString()}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </>
    );
}