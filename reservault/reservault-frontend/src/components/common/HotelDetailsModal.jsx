import React, { useEffect } from "react";
import { motion } from "framer-motion";
import { FaTimes, FaStar } from "react-icons/fa";
import { FaLocationDot } from "react-icons/fa6";
import { Fancybox } from "@fancyapps/ui";
import "@fancyapps/ui/dist/fancybox/fancybox.css";
import { useQuery } from "@tanstack/react-query";
import api from "../../api/axios";

const HotelDetailsModal = ({ hotelIdentifier, onClose }) => {
    const { data: hotel, isLoading, error } = useQuery({
        queryKey: ["hotel", hotelIdentifier],
        queryFn: async () => {
            const { data } = await api.get(`/hotels/${hotelIdentifier}`);
            return data;
        },
        enabled: !!hotelIdentifier,
    });

    useEffect(() => {
        const handleKeyDown = (e) => {
            if (e.key === "Escape") {
                onClose();
            }
        };

        window.addEventListener("keydown", handleKeyDown);
        return () => {
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [onClose]);

    useEffect(() => {
        Fancybox.bind("[data-fancybox='gallery']", {
            Thumbs: {
                autoStart: true,
            },
        });

        return () => {
            Fancybox.unbind("[data-fancybox='gallery']");
        };
    }, [hotel]);

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    if (!hotelIdentifier) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <motion.div
                className="flex flex-col justify-between bg-white rounded-lg shadow-lg p-6 w-full max-w-5xl relative"
                initial={{ opacity: 0, y: 50 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, ease: "easeOut" }}
            >
                <button
                    className="duration-200 ml-auto w-8 h-8 flex items-center justify-center rounded-lg text-[#32492D] hover:text-[#273823] hover:bg-gray-200"
                    onClick={onClose}
                >
                    <FaTimes size={20} />
                </button>

                {isLoading ? (
                    <div className="text-center text-gray-600 my-8">Loading hotel details...</div>
                ) : error ? (
                    <div className="text-center text-red-500 my-8">Failed to load hotel.</div>
                ) : (
                    <>
                        <h2 className="text-2xl font-semibold text-gray-900 text-center mb-2">
                            {hotel.name}
                        </h2>

                        <div className="flex items-center justify-center space-x-1 text-[#32492D] mb-4">
                            {Array.from({ length: hotel.stars }).map((_, i) => (
                                <FaStar key={i} size={25} />
                            ))}
                        </div>

                        <div className="flex gap-10 p-4">
                            <div className="relative w-80 h-80 flex-shrink-0 rounded-md overflow-hidden group cursor-pointer">
                                <a href={hotel.imagesUrls?.[0]} data-fancybox="gallery">
                                    <img
                                        src={hotel.imagesUrls?.[0] ?? "https://via.placeholder.com/200"}
                                        alt={hotel.name}
                                        className="w-full h-full object-cover transition duration-300 group-hover:brightness-75"
                                    />
                                    <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-black bg-opacity-30 text-white text-lg font-semibold">
                                        Show more images
                                    </div>
                                </a>

                                {hotel.imagesUrls?.slice(1).map((url, idx) => (
                                    <a
                                        key={idx}
                                        href={url}
                                        data-fancybox="gallery"
                                        className="hidden"
                                    >
                                        <img src={url} alt={`Hotel image ${idx + 2}`} />
                                    </a>
                                ))}
                            </div>

                            <div className="flex flex-col items-start space-y-4">
                                <div className="flex space-x-1">
                                    <FaLocationDot size={25} className="text-[#32492D]" />
                                    <div className="text-base text-gray-600">
                                        {hotel.location.street}, {hotel.location.postalCode} {hotel.location.city}, {hotel.location.country}
                                    </div>
                                </div>

                                <div className="text-gray-600">
                                    {limitText(hotel.description, 900)}
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </motion.div>
        </div>
    );
};

export default HotelDetailsModal;
