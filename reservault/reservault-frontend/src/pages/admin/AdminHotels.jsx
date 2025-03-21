import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext";
import { useNavigate, useParams } from "react-router-dom";
import { FaList, FaPlus } from "react-icons/fa";
import { motion } from "framer-motion";
import api from "../../api/axios";
import Header from "../../components/common/Header";
import Sidebar from "../../components/common/Sidebar";
import HotelList from "../../components/admin/HotelList";
import HotelAddForm from "../../components/admin/HotelAddForm";
import HotelModifyModal from "../../components/admin/HotelModifyModal";

const AdminHotels = () => {
    const { isAuthenticated, isAdmin, loading } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { view } = useParams();
    const [selectedHotel, setSelectedHotel] = useState(null);

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
        mutationFn: async (formData) => {
            await api.post("/admin/hotels", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["admin", "hotels"]);
            navigate("/admin/hotels/list");
        },
    });

    const updateHotelMutation = useMutation({
        mutationFn: async ({ id, formData }) => {
            await api.put(`/admin/hotels/${id}`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["admin", "hotels"]);
            setSelectedHotel(null);
        },
    });

    return (
        <>
            <Header />
            <div className="container mx-auto max-w-6xl py-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Hotels</h1>

                <div className="flex gap-6">
                    <Sidebar
                        options={[
                            { label: "List Hotels", value: "list", icon: FaList },
                            { label: "Add Hotel", value: "add", icon: FaPlus },
                        ]}
                        activeView={view || "list"}
                    />

                    <motion.div className="flex-grow rounded-md">
                        {view === "list" ? (
                            <HotelList
                                hotels={hotels}
                                isLoading={isLoading}
                                error={error}
                                onModify={(hotel) =>
                                    setSelectedHotel({
                                        ...hotel,
                                        images: hotel.imagesUrls || [],
                                    })
                                } />
                        ) : (
                            <HotelAddForm onSubmit={addHotelMutation.mutate} onCancel={() => setView("list")} />
                        )}
                    </motion.div>
                </div>
            </div>

            {selectedHotel && (
                <HotelModifyModal
                    hotel={selectedHotel}
                    onSubmit={(id, formData) => updateHotelMutation.mutate({ id, formData })}
                    onClose={() => setSelectedHotel(null)}
                />
            )}
        </>
    );
};



export default AdminHotels;