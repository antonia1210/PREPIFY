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
import "./pages/global.css"
import AdminPanel from "./pages/AdminPanel";
function App() {

    return (<Routes>
        <Route path="/" element={<Home />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/feed" element={<Feed />} />
        <Route path="/recipe/:id" element={<RecipeDetails />} />
        <Route path="/add" element={<AddRecipe />} />
        <Route path="/edit/:id" element={<AddRecipe />} />
        <Route path="/statistics" element={<Statistics />} />
        <Route path="/user-profile" element={<UserProfile />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="/chat/:userId" element={<Chat />} />
        <Route path="/admin" element={<AdminPanel />} />
        </Routes>
    );
}

export default App;