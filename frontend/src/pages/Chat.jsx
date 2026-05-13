import { useState, useEffect, useRef } from "react";
import Navbar from "./Navbar";
import "./Chat.css";
import { getUser } from "../utils/auth";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { useParams, useNavigate } from "react-router-dom";
import API_BASE from "../config.js"

export default function Chat() {
    const [conversations, setConversations] = useState([]);
    const [selectedConv, setSelectedConv] = useState(null);
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const clientRef = useRef(null);
    const messagesEndRef = useRef(null);
    const currentUser = getUser();
    const { userId } = useParams();
    const navigate = useNavigate();

    const fetchConversations = () => {
        fetch(`${API_BASE}/api/messages/conversations/${currentUser.id}`)
            .then(res => res.json())
            .then(data => setConversations(Array.isArray(data) ? data : []));
    };

    useEffect(() => {
        fetchConversations();
    }, []);

    useEffect(() => {
        if (userId && currentUser) {
            fetch(`${API_BASE}/api/users/${userId}`)
                .then(res => res.json())
                .then(user => {
                    setSelectedConv({
                        otherUserId: parseInt(userId),
                        otherUserName: user.name
                    });
                });
        }
    }, [userId]);

    useEffect(() => {
        if (!selectedConv) return;

        fetch(`${API_BASE}/api/messages/conversation?userId1=${currentUser.id}&userId2=${selectedConv.otherUserId}`)
            .then(res => res.json())
            .then(data => setMessages(data));

        const conversationId = Math.min(currentUser.id, selectedConv.otherUserId) + "_" + Math.max(currentUser.id, selectedConv.otherUserId);

        const client = new Client({
            webSocketFactory: () => new SockJS(`${API_BASE}/ws`),
            onConnect: () => {
                console.log("Chat WebSocket connected! Subscribing to:", `/topic/chat/${conversationId}`);
                client.subscribe(`/topic/chat/${conversationId}`, (msg) => {
                    console.log("Received chat message:", msg.body);
                    const received = JSON.parse(msg.body);
                    setMessages(prev => [...prev, received]);
                    fetchConversations();
                });
            },
            onStompError: (frame) => console.log("STOMP error:", frame)
        });

        client.activate();
        clientRef.current = client;

        return () => client.deactivate();
    }, [selectedConv]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const sendMessage = async () => {
        if (!newMessage.trim() || !selectedConv) return;

        const message = {
            senderId: currentUser.id,
            senderName: currentUser.name,
            receiverId: selectedConv.otherUserId,
            receiverName: selectedConv.otherUserName,
            content: newMessage
        };

        await fetch(`${API_BASE}/api/messages/send`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(message)
        });

        setNewMessage("");
        fetchConversations();
    };

    return (
        <>
            <Navbar />
            <div className="chat-page">
                <h1 className="logo">PREPIFY</h1>
                <div className="chat-container">
                    <div className="users-panel">
                        <h3>Conversations</h3>
                        {conversations.length === 0 ? (
                            <p style={{color: "#999", fontSize: "0.9rem", padding: "0.5rem"}}>
                                No conversations yet. Start one from a recipe page!
                            </p>
                        ) : (
                            conversations.map((conv, index) => (
                                <div
                                    key={index}
                                    className={`user-item ${selectedConv?.otherUserId === conv.otherUserId ? "active" : ""}`}
                                    onClick={() => setSelectedConv(conv)}
                                >
                                    <div className="user-avatar">{conv.otherUserName?.charAt(0).toUpperCase()}</div>
                                    <div className="conv-info">
                                        <span className="conv-name">{conv.otherUserName}</span>
                                        <span className="conv-last">{conv.lastMessage}</span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    <div className="chat-panel">
                        {!selectedConv ? (
                            <div className="no-chat">Select a conversation or start one from a recipe page</div>
                        ) : (
                            <>
                                <div className="chat-header">
                                    <div className="user-avatar">{selectedConv.otherUserName?.charAt(0).toUpperCase()}</div>
                                    <h3>{selectedConv.otherUserName}</h3>
                                </div>
                                <div className="messages-container">
                                    {messages.map((msg, index) => (
                                        <div key={index} className={`message ${msg.senderId === currentUser.id ? "sent" : "received"}`}>
                                            <span className="message-content">{msg.content}</span>
                                            <span className="message-time">
                                                {new Date(msg.timestamp).toLocaleTimeString()}
                                            </span>
                                        </div>
                                    ))}
                                    <div ref={messagesEndRef} />
                                </div>
                                <div className="message-input">
                                    <input
                                        placeholder="Type a message..."
                                        value={newMessage}
                                        onChange={(e) => setNewMessage(e.target.value)}
                                        onKeyDown={(e) => { if (e.key === "Enter") sendMessage(); }}
                                    />
                                    <button onClick={sendMessage}>Send</button>
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}