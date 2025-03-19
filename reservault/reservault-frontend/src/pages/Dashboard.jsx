import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import Header from "../components/common/Header";

const Dashboard = () => {
    const { user, isAuthenticated, loading } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!loading && !isAuthenticated) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, navigate]);

    if (loading) {
        return <div className="flex justify-center items-center min-h-screen text-gray-600">Loading...</div>;
    }

    return (
        <>
            <Header />
            <div className="container mx-auto flex items-center justify-center min-h-screen">
                <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md text-center">
                    <h2 className="text-2xl font-semibold text-gray-900">Welcome, {user?.name}!</h2>
                </div>
            </div>
        </>
    );
};

export default Dashboard;
