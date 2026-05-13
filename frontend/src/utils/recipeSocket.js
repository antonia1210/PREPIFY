import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";
import API_BASE from "../config.js"

export function connectRecipeSocket(onMessage) {
    const client = new Client({
        webSocketFactory: () => new SockJS(`${API_BASE}/ws`),
        reconnectDelay: 5000,
        debug: () => {},
        shouldReconnect: () => navigator.onLine,
        onConnect: () => {
            console.log("Connected to websocket");
            client.subscribe("/topic/recipes", (message) => {
                const body = JSON.parse(message.body);
                onMessage(body);
            });
        },
        onWebSocketError: () => {
            if (!navigator.onLine) {
                client.deactivate();
            }
        },
        onDisconnect: () => {
            if (!navigator.onLine) {
                client.deactivate();
            }
        }
    });

    window.addEventListener("online", () => {
        if (!client.active) client.activate();
    });

    client.activate();
    return client;
}