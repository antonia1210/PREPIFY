import { useEffect, useRef } from "react";
import { logout } from "./auth";
import { useNavigate } from "react-router-dom";

const INACTIVITY_TIMEOUT = 15 * 60 * 1000; //change if needed

export function useInactivityLogout() {
    const navigate = useNavigate();
    const timerRef = useRef(null);

    const resetTimer = () => {
        if (timerRef.current) clearTimeout(timerRef.current);
        timerRef.current = setTimeout(() => {
            logout();
            navigate("/login");
        }, INACTIVITY_TIMEOUT);
    };

    useEffect(() => {
        const events = ["mousemove", "keydown", "click", "scroll", "touchstart"];
        events.forEach(e => window.addEventListener(e, resetTimer));
        resetTimer();
        return () => {
            events.forEach(e => window.removeEventListener(e, resetTimer));
            if (timerRef.current) clearTimeout(timerRef.current);
        };
    }, []);
}