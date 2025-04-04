import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import api from "../../api/axios";

const StatCard = ({ label, count }) => (
    <div className="bg-white shadow-md border border-gray-200 rounded-lg px-6 py-4 w-64 text-center">
        <h3 className="text-lg text-gray-700 font-semibold">{label}</h3>
        <p className="text-3xl font-bold text-[#32492D] mt-2">{count}</p>
    </div>
);

const UniversalDashboard = ({ role }) => {
    const { data: stats, isLoading, error, refetch } = useQuery({
        queryKey: [role.toLowerCase(), "dashboard-stats"],
        queryFn: async () => {
            const endpoint = role === "Admin" ? "/admin/statistics" : "/manager/statistics";
            const { data } = await api.get(endpoint);
            return data;
        },
        enabled: role === "Admin" || role === "Manager",
    });

    useEffect(() => {
        if (role === "Admin" || role === "Manager") {
            refetch();
        }
    }, [role, refetch]);

    if (role !== "Admin" && role !== "Manager") return null;

    if (isLoading) {
        return (
            <div className="flex justify-center items-center text-gray-600" style={{ minHeight: `calc(100vh - 88px)` }}>
                Loading statistics...
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex justify-center items-center text-red-600" style={{ minHeight: `calc(100vh - 88px)` }}>
                Failed to load stats.
            </div>
        );
    }

    return (
        <div className="flex flex-col items-center justify-center py-12 px-4" style={{ minHeight: `calc(100vh - 88px)` }}>
            <h1 className="text-4xl font-bold text-[#32492D] mb-6">
                Welcome, {role}
            </h1>

            <motion.div
                className={`grid gap-8 ${role === "Admin" ?
                    "grid-cols-1 sm:grid-cols-2 md:grid-cols-3" :
                    "grid-cols-1 sm:grid-cols-2"}`
                }
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1, duration: 0.4, ease: "easeOut" }}
            >
                {role === "Admin" && (
                    <>
                        <StatCard label="Total Users" count={stats.totalUsers} />
                        <StatCard label="Total Managers" count={stats.totalManagers} />
                        <StatCard label="Total Offers" count={stats.totalOffers} />
                        <StatCard label="Total Hotels" count={stats.totalHotels} />
                        <StatCard label="Verified Managers" count={stats.verifiedManagers} />
                        <StatCard label="Total Bookings" count={stats.totalBookings} />
                    </>
                )}

                {role === "Manager" && (
                    <>
                        <StatCard label="Offers" count={stats.offers} />
                        <StatCard label="Bookings" count={stats.bookings} />
                        <StatCard label="Reviews" count={stats.reviews} />
                        <StatCard label="Review Response Rate" count={`${stats.responseRate.toFixed(1)}%`} />
                    </>
                )}
            </motion.div>
        </div>
    );
};

export default UniversalDashboard;