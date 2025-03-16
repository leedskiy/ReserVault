import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { FaList, FaPlus } from "react-icons/fa";
import { motion } from "framer-motion";
import api from "../api/axios";
import Header from "../components/Header";
import Sidebar from "../components/Sidebar";

const AdminHotels = () => {
    const { isAuthenticated, isAdmin, loading } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [view, setView] = useState("list");

    useEffect(() => {
        if (!loading && (!isAuthenticated || !isAdmin)) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, isAdmin, navigate]);

    const { data: hotels, isLoading, error } = useQuery({
        queryKey: ["admin", "hotels"],
        queryFn: async () => {
            const { data } = await api.get("/admin/hotels");
            return data;
        },
        retry: false,
    });

    const addHotelMutation = useMutation({
        mutationFn: async (newHotel) => {
            const formData = new FormData();
            formData.append("hotel", JSON.stringify(newHotel));

            await api.post("/admin/hotels", formData);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["admin", "hotels"]);
            setView("list");
        },
    });

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    return (
        <>
            <Header />
            <div className="container mx-auto max-w-6xl py-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Hotels</h1>

                <div className="flex gap-6">
                    <Sidebar
                        options={[
                            { label: "List Hotels", value: "list", icon: FaList },
                            { label: "Add New Hotel", value: "add", icon: FaPlus },
                        ]}
                        activeView={view}
                        setActiveView={setView}
                    />

                    <motion.div className="flex-grow rounded-md">
                        {view === "list" ? (
                            <>
                                {isLoading && (
                                    <div className="flex justify-center items-center min-h-screen text-gray-600">
                                        Loading hotels...
                                    </div>
                                )}

                                {error && (
                                    <div className="flex justify-center items-center min-h-screen text-red-500">
                                        Failed to load hotels.
                                    </div>
                                )}

                                <motion.div
                                    className="space-y-6 flex flex-col items-center"
                                    initial="hidden"
                                    animate="visible"
                                    variants={{
                                        hidden: { opacity: 0 },
                                        visible: { opacity: 1, transition: { staggerChildren: 0.2 } },
                                    }}
                                >
                                    {hotels?.map((hotel, index) => (
                                        <motion.div
                                            key={hotel.id}
                                            className="bg-white shadow-md rounded-lg overflow-hidden flex border border-gray-200 p-4"
                                            initial={{ opacity: 0, y: 30 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            transition={{ delay: index * 0.1, duration: 0.4, ease: "easeOut" }}
                                        >
                                            <div className="w-64 h-64 flex-shrink-0 rounded-md overflow-hidden">
                                                <img
                                                    src={hotel.imagesUrls[0] || "https://via.placeholder.com/200"}
                                                    alt={hotel.name}
                                                    className="w-full h-full object-cover"
                                                />
                                            </div>

                                            <div className="flex-grow px-6 flex flex-col space-y-3">
                                                <h2 className="text-lg font-semibold text-gray-900">
                                                    {limitText(hotel.name, 35)}
                                                </h2>
                                                <p className="text-sm text-gray-600">
                                                    {limitText(`${hotel.location.city}, ${hotel.location.country}`, 40)}
                                                </p>
                                                <p className="text-sm text-gray-700 mt-2">
                                                    {limitText(hotel.description, 500)}
                                                </p>
                                            </div>

                                            <div className="flex items-start w-16 justify-center">
                                                <span className="bg-yellow-700 text-white px-2 py-2 rounded-lg font-bold text-sm">
                                                    ⭐{hotel.stars}
                                                </span>
                                            </div>
                                        </motion.div>
                                    ))}
                                </motion.div>
                            </>
                        ) : (
                            <HotelForm onSubmit={addHotelMutation.mutate} onCancel={() => setView("list")} />
                        )}
                    </motion.div>
                </div>
            </div>
        </>
    );
};


const HotelForm = ({ onSubmit, onCancel }) => {
    const [hotelData, setHotelData] = useState({
        name: "",
    });

    const handleChange = (e) => {
        setHotelData({ ...hotelData, name: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(hotelData);
    };

    return (
        <motion.div
            className="flex-grow bg-white p-6 rounded-lg shadow-md flex flex-col justify-center items-center"
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
        >
            <h2 className="text-xl font-bold mb-6">Add New Hotel</h2>
            <form onSubmit={handleSubmit} className="w-full max-w-lg space-y-4">
                <input
                    name="name"
                    placeholder="Hotel Name"
                    className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    onChange={handleChange}
                    value={hotelData.name}
                />

                <div className="flex justify-end space-x-2">
                    <button
                        type="button"
                        className="px-4 py-2 border rounded-md bg-gray-200 hover:bg-gray-300"
                        onClick={onCancel}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="px-4 py-2 border rounded-md bg-blue-500 text-white hover:bg-blue-600"
                    >
                        Add Hotel
                    </button>
                </div>
            </form>
        </motion.div>
    );
};

export default AdminHotels;
