import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../context/AuthContext";
import { useNavigate, useParams } from "react-router-dom";
import { FaList, FaPlus } from "react-icons/fa";
import { motion } from "framer-motion";
import api from "../../api/axios";
import Header from "../../components/common/Header";
import Sidebar from "../../components/common/Sidebar";
import OfferList from "../../components/common/OfferList";
import OfferAddForm from "../../components/manager/OfferAddForm";
import OfferModifyModal from "../../components/manager/OfferModifyModal";

const ManagerOffers = () => {
    const { isAuthenticated, isManager, loading } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { view } = useParams();
    const [selectedOffer, setSelectedOffer] = useState(null);

    useEffect(() => {
        if (!loading && (!isAuthenticated || !isManager)) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, isManager, navigate]);

    const { data: offers, isLoading, error } = useQuery({
        queryKey: ["manager", "offers"],
        queryFn: async () => {
            const { data } = await api.get("/manager/offers");
            return data;
        },
        retry: false,
    });

    const addOfferMutation = useMutation({
        mutationFn: async (formData) => {
            await api.post("/manager/offers", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["manager", "offers"]);
            navigate("/manager/offers/list");
        },
    });

    const updateOfferMutation = useMutation({
        mutationFn: async ({ id, formData }) => {
            await api.put(`/manager/offers/${id}`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
        },
        onSuccess: () => {
            queryClient.invalidateQueries(["manager", "offers"]);
            setSelectedOffer(null);
        },
    });

    return (
        <>
            <Header />
            <div className="container mx-auto max-w-6xl py-6">
                <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">Offers</h1>

                <div className="flex gap-6">
                    <Sidebar
                        options={[
                            { label: "List Offers", value: "list", icon: FaList },
                            { label: "Add Offer", value: "add", icon: FaPlus },
                        ]}
                        activeView={view || "list"}
                        basePath="manager/offers"
                    />

                    <motion.div className="flex-grow rounded-md">
                        {view === "list" ? (
                            <OfferList
                                offers={offers}
                                isLoading={isLoading}
                                error={error}
                                onModify={(offer) =>
                                    setSelectedOffer({
                                        ...offer,
                                        images: offer.imagesUrls || [],
                                    })
                                }
                            />
                        ) : (
                            <OfferAddForm onSubmit={addOfferMutation.mutate} onCancel={() => setView("list")} />
                        )}
                    </motion.div>
                </div>
            </div>

            {selectedOffer && (
                <OfferModifyModal
                    offer={selectedOffer}
                    onSubmit={(id, formData) => updateOfferMutation.mutate({ id, formData })}
                    onClose={() => setSelectedOffer(null)}
                />
            )}
        </>
    );
};

export default ManagerOffers;