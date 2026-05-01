import Navbar from "./Navbar";
import './Feed.css'
import {useNavigate} from 'react-router-dom'
import {useEffect, useState} from "react";
import { useRef } from "react";
import {getCookie, setCookie} from "../utils/cookies";
import { useOnlineStatus } from "../utils/useOnlineStatus";
import { useOfflineQueue } from "../utils/useOfflineQueue";
import { connectRecipeSocket } from "../utils/recipeSocket";

export default function Feed() {
    const navigate = useNavigate();

    const [recipes, setRecipes] = useState([]);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loading, setLoading] = useState(false);
    const prefetchedRef = useRef({});
    const observerRef = useRef(null);
    const sentinelRef = useRef(null);

    const recipesPerPage = 10;
    const [query, setQuery] = useState("");
    const [filterOpen, setFilterOpen] = useState(false);
    const isOnline = useOnlineStatus();
    const {enqueue} = useOfflineQueue();
    const [localRecipes, setLocalRecipes] = useState([]);
    const [category, setCategory] = useState("");
    const [sortBy, setSortBy] = useState("");

    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        const stored = localStorage.getItem("user");
        if (stored) setCurrentUser(JSON.parse(stored));
    }, []);

    useEffect(() => {
        const init = async () => {
            prefetchedRef.current = {};
            setRecipes([]);
            setPage(0);
            setHasMore(true);

            if (!isOnline) {
                const keys = Object.keys(localStorage).filter(k => k.startsWith("recipe_"));
                const cached = keys.map(k => JSON.parse(localStorage.getItem(k)));
                setRecipes(cached);
                setHasMore(false);
                return;
            }

            try {
                if (category || sortBy) {
                    const params = new URLSearchParams();
                    if (category) params.append("category", category);
                    if (sortBy) params.append("sortBy", sortBy);
                    const response = await fetch(`http://localhost:8080/api/recipes/filter?${params.toString()}`);
                    const data = await response.json();
                    if (Array.isArray(data)) {
                        setRecipes(data);
                        setHasMore(false);
                    }
                    return;
                }
                if (query) {
                    const response = await fetch(`http://localhost:8080/api/recipes/search?query=${query}`);
                    const data = await response.json();
                    if (Array.isArray(data)) {
                        setRecipes(data);
                        setHasMore(false);
                    }
                    return;
                }

                const response = await fetch(`http://localhost:8080/api/recipes?page=0&size=${recipesPerPage}`);
                const data = await response.json();
                prefetchedRef.current[0] = data;
                if (data.length < recipesPerPage) setHasMore(false);
                data.forEach(r => localStorage.setItem(`recipe_${r.id}`, JSON.stringify(r)));
                setRecipes(data);
                setPage(1);

                const cached = JSON.parse(localStorage.getItem("localRecipes") || "[]");
                if (cached.length > 0) {
                    setRecipes(prev => {
                        const existingIds = new Set(prev.map(r => r.id));
                        return [...prev, ...cached.filter(r => !existingIds.has(r.id))];
                    });
                }

                fetchPage(1);
            } catch (error) {
                console.error("Error loading recipes:", error);
                const keys = Object.keys(localStorage).filter(k => k.startsWith("recipe_"));
                const cached = keys.map(k => JSON.parse(localStorage.getItem(k)));
                setRecipes(cached);
                setHasMore(false);
            }
        };
        init();
        const client = connectRecipeSocket(() => {
            if (!query && !category && !sortBy) {
                init();
            }
        });

        return () => client.deactivate();
    }, [query, category, sortBy]);


    useEffect(() => {
        if (observerRef.current) observerRef.current.disconnect();
        observerRef.current = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && hasMore && !loading) {
                loadMore();
            }
        }, { threshold: 0.1 });
        if (sentinelRef.current) observerRef.current.observe(sentinelRef.current);
        return () => observerRef.current?.disconnect();
    }, [hasMore, loading, page]);


    const fetchPage = async (pageNum) => {
        if (prefetchedRef.current[pageNum]) {
            return prefetchedRef.current[pageNum];
        }
        const response = await fetch(`http://localhost:8080/api/recipes?page=${pageNum}&size=${recipesPerPage}`);
        const data = await response.json();
        prefetchedRef.current[pageNum] = data;
        return data;
    };

    const prefetchNext = (pageNum) => {
        if (!prefetchedRef.current[pageNum]) {
            fetchPage(pageNum);
        }
    };

    const loadMore = async () => {
        if (loading || !hasMore) return;
        if (!isOnline) {
            setRecipes(localRecipes);
            return;
        }
        setLoading(true);
        try {
            const data = await fetchPage(page);
            if (data.length < recipesPerPage) setHasMore(false);
            setRecipes(prev => {
                const existingIds = new Set(prev.map(r => r.id));
                const newRecipes = data.filter(r => !existingIds.has(r.id));
                newRecipes.forEach(r => localStorage.setItem(`recipe_${r.id}`, JSON.stringify(r)));
                return [...prev, ...newRecipes];
            });
            setLocalRecipes(prev => [...prev, ...data]);
            setPage(prev => {
                prefetchNext(prev + 1);
                return prev + 1;
            });
        } catch (error) {
            console.error("Error loading recipes:", error);
            setRecipes(localRecipes);
        }
        setLoading(false);
    };

    const handleDelete = async (id) => {
        const confirmDelete = window.confirm("Are you sure you want to delete this recipe?");
        if (!confirmDelete) return;

        if (!isOnline) {
            enqueue({ type: "DELETE", id });
            setRecipes(prev => prev.filter(r => r.id !== id));
            setLocalRecipes(prev => prev.filter(r => r.id !== id));
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/recipes/${id}`, { method: "DELETE" });
            if (!response.ok) throw new Error("Failed to delete");
            setRecipes(prev => prev.filter(r => r.id !== id));
            localStorage.removeItem(`recipe_${id}`);
        } catch (error) {
            console.error("Error deleting:", error);
        }
    };
    const startGenerator = async () => {
        await fetch("http://localhost:8080/api/recipes/generator/start", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                batchSize: 3,
                intervalMillis: 5000
            })
        });
    };

    const stopGenerator = async () => {
        await fetch("http://localhost:8080/api/recipes/generator/stop", {
            method: "POST"
        });
    };


    return(
        <>
        <Navbar />
            {!isOnline && (
                <div style={{
                    background: "#c0392b",
                    color: "white",
                    textAlign: "center",
                    padding: "0.5rem",
                    fontWeight: "bold"
                }}>
                    ⚠️ You are offline — changes will sync when connection is restored
                </div>
            )}
        <div className="feed-container">
            <div style={{ marginBottom: "1rem" }}>
                <button onClick={startGenerator}>Start Auto Generate</button>
                <button onClick={stopGenerator} style={{ marginLeft: "10px" }}>Stop Auto Generate</button>
            </div>
            {filterOpen && (
                <div className="filter-overlay" onClick={() => setFilterOpen(false)} />
            )}
            <div className={`filter-panel ${filterOpen ? "open" : ""}`}>
                <div className="filter-header">
                    <h3>FILTERS</h3>
                    <button onClick={() => setFilterOpen(false)}>✕</button>
                </div>
                <div className="filter-section">
                    <h4>Category</h4>
                    <select value={category} onChange={(e) => { setCategory(e.target.value);  }}>
                        <option value="">All</option>
                        <option value="Breakfast">Breakfast</option>
                        <option value="Lunch">Lunch</option>
                        <option value="Dinner">Dinner</option>
                        <option value="Dessert">Dessert</option>
                    </select>
                </div>
                <div className="filter-section">
                    <h4>Sort By</h4>
                    <select value={sortBy} onChange={(e) => { setSortBy(e.target.value);  }}>
                        <option value="">Default</option>
                        <option value="rating">Top Rated</option>
                        <option value="prepTime">Prep Time</option>
                        <option value="servings">Servings</option>
                    </select>
                </div>
                <button className="clear-filters" onClick={() => { setCategory(""); setSortBy(""); }}>CLEAR</button>
            </div>
            <h1 className="logo">PREPIFY</h1>
            <div className="controls">
                <div className="left-controls">
                    <input placeholder="search..."
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                    />
                    <button className="sort-filter" onClick={() => setFilterOpen(true)}>Filters</button>
                </div>
                <div className="right-controls">
                    <button className="add-button" onClick={() => navigate("/add")}> + Add Recipe</button>
                </div>

            </div>
            <div className="table-wrapper">
            <table className="recipe-table">
                <thead>
                <tr>
                    <th></th>
                    <th>recipe</th>
                    <th>category</th>
                    <th>servings</th>
                    <th>actions</th>
                </tr>
                </thead>

                <tbody>
                {recipes.map((recipe) => (
                    <tr key={recipe.id}>
                        <td  onClick={() => navigate(`/recipe/${recipe.id}`)}
                             style={{cursor: "pointer"}}>
                            <img
                                src={recipe.image}
                                alt={recipe.name}
                                className="recipe-img"
                            />
                        </td>
                        <td
                            onClick={() => navigate(`/recipe/${recipe.id}`)}
                            style={{cursor: "pointer"}}
                            >
                            {recipe.name}
                        </td>
                        <td>{recipe.category}</td>
                        <td>{recipe.servings}</td>
                        <td>
                            <div className="actions">
                            <label className="view-button" onClick={() => navigate(`/recipe/${recipe.id}`)}
                                   style={{cursor: "pointer", textDecoration: "underline"}}>
                                View
                            </label>
                            {(currentUser?.id == recipe.userId ||!recipe.userId )&& (
                                    <>
                                <label className="edit-button" onClick={() => navigate(`/edit/${recipe.id}`)}
                                       style={{cursor: "pointer", textDecoration: "underline"}}>
                                    Edit</label>
                                    <label
                                        className="delete-button"
                                        onClick={() => handleDelete(recipe.id)}
                                        style={{ cursor: "pointer", textDecoration: "underline" }}
                                    >
                                        Delete
                                    </label>
                            </>
                            )}
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            </div>
            <div ref={sentinelRef} style={{ height: "40px", textAlign: "center", padding: "1rem" }}>
                {loading && <span>Loading...</span>}
                {!hasMore && <span>No more recipes</span>}
            </div>
        </div>
            </>
    )
}