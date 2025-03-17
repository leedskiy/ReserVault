import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { useNavigate, useParams } from "react-router-dom";
import { FaList, FaPlus, FaTimes } from "react-icons/fa";
import { motion } from "framer-motion";
import api from "../api/axios";
import Header from "../components/Header";
import Sidebar from "../components/Sidebar";
import HotelList from "../components/hotels/HotelList";
import HotelForm from "../components/hotels/HotelForm";

const AdminHotels = () => {
    const { isAuthenticated, isAdmin, loading } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { view } = useParams();

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
                            <HotelList hotels={hotels} isLoading={isLoading} error={error} />
                        ) : (
                            <HotelForm onSubmit={addHotelMutation.mutate} onCancel={() => setView("list")} />
                        )}
                    </motion.div>
                </div>
            </div>
        </>
    );
};



export default AdminHotels;