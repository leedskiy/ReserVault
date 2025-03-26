import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import Header from "../components/common/Header";
import SmartOfferSearch from "../components/user/SmartOfferSearch";

const Dashboard = () => {
    const { user, isAuthenticated, loading, isUser } = useAuth();
    const navigate = useNavigate();

    const headerRef = useRef(null);
    const [headerHeight, setHeaderHeight] = useState(0);

    useEffect(() => {
        if (!loading && !isAuthenticated) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, navigate]);

    useEffect(() => {
        if (headerRef.current) {
            setHeaderHeight(headerRef.current.offsetHeight);
        }
    }, []);

    if (loading) {
        return <div className="flex justify-center items-center min-h-screen text-gray-600">Loading...</div>;
    }

    if (!isUser) {
        return (
            <>
                <Header />
                <div className="container mx-auto flex items-center justify-center" style={{ minHeight: `calc(100vh - 88px)` }}>
                    <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md text-center">
                        <h2 className="text-2xl font-semibold text-gray-900">Welcome, {user?.name}!</h2>
                    </div>
                </div>
            </>
        );
    }

    return (
        <>
            <Header />
            <div className="flex flex-col container mx-auto justify-center max-w-4xl" style={{ minHeight: `calc(100vh - 88px)` }}>
                <h1 className="text-4xl font-bold text-[#32492D] mb-6">
                    Search for Accommodation
                </h1>
                <SmartOfferSearch />
            </div>
        </>
    );
};

export default Dashboard;