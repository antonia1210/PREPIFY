import Navbar from "./Navbar";
import {useState, useEffect} from "react";
import {PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer} from "recharts";
const COLORS = ["#E5989B", "#B5C9D9", "#C08497", "#EED6D3", "#F6BD60", "#A8D5BA"];
import "./Statistics.css"
import { connectRecipeSocket } from "../utils/recipeSocket";
import API_BASE from "../config.js"
import {authHeaders} from "../utils/auth";

function getAverage(ratings) {
    if (!ratings || ratings.length === 0) return 0;
    return ratings.reduce((a, b) => a + b, 0) / ratings.length;
}

export default function Statistics() {
    const [categoryData, setCategoryData] = useState([]);
    const [recipes, setRecipes] = useState([]);
    const [totalCount, setTotalCount] = useState(0);
    const [averageRating, setAverageRating] = useState(0);
    const [userRecipeData, setUserRecipeData] = useState([]);

    const fetchStatistics = async () => {
        try {
            const recipesResponse = await fetch(`${API_BASE}/api/recipes?page=0&size=100`, {headers: authHeaders()});
            const recipesData = await recipesResponse.json();
            setRecipes(recipesData);

            const [categoryResponse, countResponse, averageResponse] = await Promise.all([
                fetch(`${API_BASE}/api/recipes/stats/by-category`, {headers: authHeaders()}),
                fetch(`${API_BASE}/api/recipes/stats/total-count`, {headers: authHeaders()}),
                fetch(`${API_BASE}/api/recipes/stats/average-rating`, {headers: authHeaders()}),
            ]);

            const categoryMap = await categoryResponse.json();
            const count = await countResponse.json();
            const average = await averageResponse.json();

            const formattedCategoryData = Object.keys(categoryMap).map((key) => ({
                name: key,
                value: categoryMap[key],
            }));

            setCategoryData(formattedCategoryData);
            setTotalCount(count);
            setAverageRating(average);

            // Only load real users (not faker) for the per-user chart
            const usersResponse = await fetch(`${API_BASE}/api/users`, {headers: authHeaders()});
            const usersData = await usersResponse.json();

            // Filter out faker users, keep only real ones
            const realUsers = usersData.filter(u => !u.username.startsWith("faker_"));

            const userCounts = await Promise.all(
                realUsers.map(async (user) => {
                    const countRes = await fetch(`${API_BASE}/api/recipes/user/${user.id}/count`, {headers: authHeaders()});
                    const count = await countRes.json();
                    return { name: user.username, value: count };
                })
            );

            // Sort by recipe count and take top 10
            const sorted = userCounts
                .filter(u => u.value > 0)
                .sort((a, b) => b.value - a.value)
                .slice(0, 10);

            setUserRecipeData(sorted);

        } catch (error) {
            console.error("Error fetching statistics:", error);
        }
    };

    useEffect(() => {
        fetchStatistics();
        const client = connectRecipeSocket(() => {
            fetchStatistics();
        });
        return () => client.deactivate();
    }, []);

    return (
        <>
            <Navbar />
            <div className="statistics-page">
                <h1 className="logo">PREPIFY</h1>
                <h2 className="title">STATISTICS</h2>
                <div className="statistics-container">
                    <div className="chart-view">
                        <h3>Recipe Category Distribution</h3>
                        <ResponsiveContainer width="100%" height={300}>
                            <PieChart>
                                <Pie
                                    data={categoryData}
                                    dataKey="value"
                                    nameKey="name"
                                    outerRadius={100}
                                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                    isAnimationActive={true}
                                    animationDuration={800}
                                >
                                    {categoryData.map((entry, index) => (
                                        <Cell key={index} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                        <hr />
                        <h3>Top Rated Recipes</h3>
                        {recipes
                            .filter(r => r.ratings && r.ratings.length > 0)
                            .sort((a, b) => getAverage(b.ratings) - getAverage(a.ratings))
                            .slice(0, 4)
                            .map(r => (
                                <div key={r.id} className="rating-row">
                                    <div className="rating-bar-container">
                                        <div
                                            className="rating-bar"
                                            style={{ "--width": `${(getAverage(r.ratings) / 5) * 100}%` }}
                                        ></div>
                                    </div>
                                    <span>⭐ {getAverage(r.ratings).toFixed(2)}</span>
                                    <span>{r.name}</span>
                                </div>
                            ))}
                        <hr />
                        <h3>Top 10 Users by Recipes</h3>
                        <ResponsiveContainer width="100%" height={300}>
                            <PieChart>
                                <Pie
                                    data={userRecipeData}
                                    dataKey="value"
                                    nameKey="name"
                                    outerRadius={100}
                                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                    isAnimationActive={true}
                                    animationDuration={800}
                                >
                                    {userRecipeData.map((entry, index) => (
                                        <Cell key={index} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>

                    <div className="table-view">
                        <h3>Recipe Category Distribution</h3>
                        <div className="table-wrapper">
                            <table>
                                <thead>
                                <tr>
                                    <th>Category</th>
                                    <th>Recipes</th>
                                </tr>
                                </thead>
                                <tbody>
                                {categoryData.map((item, index) => (
                                    <tr key={index}>
                                        <td>{item.name}</td>
                                        <td>{item.value}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                        <hr />
                        <h3>Top Rated Recipes</h3>
                        <div className="table-wrapper">
                            <table>
                                <thead>
                                <tr>
                                    <th>Recipe</th>
                                    <th>Category</th>
                                    <th>Rating</th>
                                </tr>
                                </thead>
                                <tbody>
                                {recipes
                                    .filter(r => r.ratings && r.ratings.length > 0)
                                    .sort((a, b) => getAverage(b.ratings) - getAverage(a.ratings))
                                    .slice(0, 5)
                                    .map(r => (
                                        <tr key={r.id}>
                                            <td>{r.name}</td>
                                            <td>{r.category}</td>
                                            <td>⭐ {getAverage(r.ratings).toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <hr />
                        <h3>Top 10 Users by Recipes</h3>
                        <div className="table-wrapper">
                            <table>
                                <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Recipes</th>
                                </tr>
                                </thead>
                                <tbody>
                                {userRecipeData.map((item, index) => (
                                    <tr key={index}>
                                        <td>{item.name}</td>
                                        <td>{item.value}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}