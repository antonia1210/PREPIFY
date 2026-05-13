import { useEffect, useState } from "react";
import Navbar from "./Navbar";
import { isAdmin } from "../utils/auth";
import { useNavigate } from "react-router-dom";
import API_BASE from "../config.js";
import "./AdminPanel.css";

export default function AdminPanel() {
    const [suspicious, setSuspicious] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAdmin()) {
            navigate("/feed");
            return;
        }
        fetch(`${API_BASE}/api/admin/suspicious`)
            .then(res => res.json())
            .then(data => setSuspicious(data));
    }, []);

    return (
        <>
            <div style={{background: "#F5EFE6", minHeight: "100vh"}}>
            <Navbar />
            <div className="admin-container">
                <h2 className="admin-title"> Suspicious Users</h2>
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