import { useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useState, useEffect, useCallback } from "react";
import { useAuth } from "../../context/AuthContext";
import { useNavigate, useParams } from "react-router-dom";
import { motion } from "framer-motion";
import { FaStar } from "react-icons/fa";
import api from "../../api/axios";
import SmartOfferSearch from "../../components/user/SmartOfferSearch";
import OfferList from "../../components/common/OfferList";
import Header from "../../components/common/Header";
import OfferSearchSidebar from "../../components/user/OfferSearchSidebar";
import HotelDetailsModal from "../../components/common/HotelDetailsModal";

const OfferSearchResults = () => {
    const { isAuthenticated, isUser, loading } = useAuth();
    const navigate = useNavigate();
    const [selectedHotelModal, setSelectedHotelModal] = useState(null);

    useEffect(() => {
        if (!loading && (!isAuthenticated || !isUser)) {
            navigate("/login");
        }
    }, [loading, isAuthenticated, isUser, navigate]);

    const [searchParams, setSearchParams] = useSearchParams();

    const {
        data: offers,
        isLoading,
        error,
    } = useQuery({
        queryKey: ["offers", "search", searchParams.toString()],
        queryFn: async () => {
            const { data } = await api.get(`/offers/search?${searchParams.toString()}`);
            return data;
        },
    });

    const hotelId = searchParams.get("hotelId");
    const selectedHotel = hotelId
        ? offers?.find((offer) => offer.hotelIdentifier === hotelId)
        : null;

    const handleApplyFilters = useCallback((filterData) => {
        const {
            sortingOption,
            filters: {
                minPrice,
                maxPrice,
                wifi,
                parking,
                pool,
                airConditioning,
                breakfast,
                rating,
                hotelStars
            },
        } = filterData;

        const updatedParams = new URLSearchParams(searchParams);

        const sortingMap = {
            "Price (Low to High)": { sortBy: "price", sortOrder: "asc" },
            "Price (High to Low)": { sortBy: "price", sortOrder: "desc" },
            "Offer Rating (Low to High)": { sortBy: "rating", sortOrder: "asc" },
            "Offer Rating (High to Low)": { sortBy: "rating", sortOrder: "desc" },
            "Hotel Stars (Low to High)": { sortBy: "stars", sortOrder: "asc" },
            "Hotel Stars (High to Low)": { sortBy: "stars", sortOrder: "desc" },
        };

        if (sortingOption && sortingMap[sortingOption]) {
            const { sortBy, sortOrder } = sortingMap[sortingOption];
            updatedParams.set("sortBy", sortBy);
            updatedParams.set("sortOrder", sortOrder);
        } else {
            updatedParams.delete("sortBy");
            updatedParams.delete("sortOrder");
        }

        if (minPrice !== undefined) updatedParams.set("minPrice", minPrice);
        if (maxPrice !== undefined) updatedParams.set("maxPrice", maxPrice);
        if (wifi !== undefined) updatedParams.set("wifi", wifi);
        if (parking !== undefined) updatedParams.set("parking", parking);
        if (pool !== undefined) updatedParams.set("pool", pool);
        if (airConditioning !== undefined) updatedParams.set("airConditioning", airConditioning);
        if (breakfast !== undefined) updatedParams.set("breakfast", breakfast);
        if (rating?.length) updatedParams.set("rating", rating[0]);
        else updatedParams.delete("rating");
        if (hotelStars) updatedParams.set("hotelStars", hotelStars);
        else updatedParams.delete("hotelStars");

        setSearchParams(updatedParams);
    }, [searchParams, setSearchParams]);

    return (
        <>
            <Header />

            <div className="container mx-auto py-8 max-w-6xl flex flex-col gap-6">
                <SmartOfferSearch />

                {selectedHotel && (
                    <motion.div
                        className="flex flex-col items-center justify-center border border-gray-200 shadow-md p-4 rounded-md bg-white text-gray-700 space-y-4"
                        initial={{ opacity: 0, y: 30 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.4, ease: "easeOut" }}
                    >
                        <h2
                            className="text-2xl font-bold text-[#32492D] hover:text-[#273823] transition-all duration-200 ease-in-out transform cursor-pointer"
                            title="Open hotel details"
                            onClick={() => setSelectedHotelModal(selectedHotel.hotelIdentifier)}
                        >
                            {selectedHotel.hotelName}
                        </h2>
                        <div className="flex items-center space-x-1">
                            {Array.from({ length: selectedHotel.stars }).map((_, i) => (
                                <FaStar size={20} key={i} className="text-[#32492D]" />
                            ))}
                        </div>
                        <div>
                            {selectedHotel.location?.city}, {selectedHotel.location?.country}
                        </div>
                    </motion.div>
                )}

                <div className="flex gap-6">
                    <OfferSearchSidebar
                        onApplyFilters={handleApplyFilters}
                        setSearchParams={setSearchParams}
                    />

                    <div className="w-full">
                        <OfferList
                            offers={offers}
                            isLoading={isLoading}
                            error={error}
                            onModify={null}
                            variant="user"
                        />
                    </div>
                </div>
            </div>

            {selectedHotelModal && (
                <HotelDetailsModal
                    hotelIdentifier={selectedHotelModal}
                    onClose={() => setSelectedHotelModal(null)}
                />
            )}
        </>
    );
};

export default OfferSearchResults;
