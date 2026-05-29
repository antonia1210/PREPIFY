import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Feed from "./pages/Feed.jsx";
import RecipeDetails from "./pages/RecipeDetails.jsx";
import AddRecipe from "./pages/AddRecipe.jsx";
import Statistics from "./pages/Statistics.jsx";
import UserProfile from "./pages/UserProfile.jsx";
import Chat from "./pages/Chat.jsx";
import AdminPanel from "./pages/AdminPanel";
import ForgotPassword from "./pages/ForgotPassword";
import ProtectedRoute from "./utils/ProtectedRoute";
import "./pages/global.css";
import ResetPassword from "./pages/ResetPassword";

function App() {
    return (
        <Routes>
            {/* Public routes */}
            <Route path="/" element={<Home />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            {/* Protected routes */}
            <Route path="/feed" element={<ProtectedRoute><Feed /></ProtectedRoute>} />
            <Route path="/recipe/:id" element={<ProtectedRoute><RecipeDetails /></ProtectedRoute>} />
            <Route path="/add" element={<ProtectedRoute><AddRecipe /></ProtectedRoute>} />
            <Route path="/edit/:id" element={<ProtectedRoute><AddRecipe /></ProtectedRoute>} />
            <Route path="/statistics" element={<ProtectedRoute><Statistics /></ProtectedRoute>} />
            <Route path="/user-profile" element={<ProtectedRoute><UserProfile /></ProtectedRoute>} />
            <Route path="/chat" element={<ProtectedRoute><Chat /></ProtectedRoute>} />
            <Route path="/chat/:userId" element={<ProtectedRoute><Chat /></ProtectedRoute>} />
            <Route path="/admin" element={<ProtectedRoute><AdminPanel /></ProtectedRoute>} />
        </Routes>
    );
}

export default App;