import { useAuth } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import Header from "../../components/common/Header";
import SmartOfferSearch from "../../components/user/SmartOfferSearch";
import UniversalDashboard from "../../components/common/UniversalDashboard";

const Dashboard = () => {
    const { user, isAuthenticated, loading, isAdmin, isManager, isUser } = useAuth();
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


    if (isAdmin || isManager) {
        return (
            <>
                <Header />
                <UniversalDashboard role={isAdmin ? "Admin" : "Manager"} />
            </>
        );
    }

    return (
        <>
            <Header />
            <div className="flex flex-col container mx-auto max-w-5xl py-64" style={{ minHeight: `calc(100vh - 88px)` }}>
                <h1 className="text-4xl font-bold text-[#32492D] mb-6">
                    Search for Accommodation
                </h1>
                <SmartOfferSearch />
            </div >
        </>
    );
};

export default Dashboard;