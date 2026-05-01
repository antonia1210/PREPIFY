import { useEffect } from "react";
import { useOnlineStatus } from "./useOnlineStatus";

export function useOfflineQueue() {
    const isOnline = useOnlineStatus();

    const enqueue = (operation) => {
        const existing = JSON.parse(localStorage.getItem("offlineQueue") || "[]");
        existing.push({ ...operation, tempId: Date.now() });
        localStorage.setItem("offlineQueue", JSON.stringify(existing));
    };

    useEffect(() => {
        if (!isOnline) return;
        const sync = async () => {
            const pending = JSON.parse(localStorage.getItem("offlineQueue") || "[]");
            localStorage.setItem("offlineQueue", JSON.stringify([]));

            for (const op of pending) {
                try {
                    if (op.type === "CREATE") {
                        await fetch("http://localhost:8080/api/recipes", {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify(op.payload)
                        });
                        const cached = JSON.parse(localStorage.getItem("localRecipes") || "[]");
                        localStorage.setItem("localRecipes", JSON.stringify(
                            cached.filter(r => !r.id?.toString().startsWith("temp_"))
                        ));
                    } else if (op.type === "UPDATE") {
                        await fetch(`http://localhost:8080/api/recipes/${op.id}`, {
                            method: "PUT",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify(op.payload)
                        });
                        localStorage.setItem(`recipe_${op.id}`, JSON.stringify(op.payload));
                    } else if (op.type === "DELETE") {
                        await fetch(`http://localhost:8080/api/recipes/${op.id}`, {
                            method: "DELETE"
                        });
                        localStorage.removeItem(`recipe_${op.id}`);
                    }
                } catch (e) {
                    const current = JSON.parse(localStorage.getItem("offlineQueue") || "[]");
                    current.push(op);
                    localStorage.setItem("offlineQueue", JSON.stringify(current));
                    console.error("Sync failed, re-queued:", op);
                }
            }
        };
        sync();
    }, [isOnline]);

    return { enqueue };
}