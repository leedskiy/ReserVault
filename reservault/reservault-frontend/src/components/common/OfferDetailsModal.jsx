import React, { useEffect } from "react";
import { motion } from "framer-motion";
import { FaTimes, FaStar, FaBed, FaUserFriends } from "react-icons/fa";
import { FaLocationDot } from "react-icons/fa6";
import { useQuery } from "@tanstack/react-query";
import FacilityIcons from "../common/FacilityIcons";
import { Fancybox } from "@fancyapps/ui";
import "@fancyapps/ui/dist/fancybox/fancybox.css";
import api from "../../api/axios";

const OfferDetailsModal = ({ offerId, onClose, onHotelClick }) => {
    const { data: offer, isLoading, error } = useQuery({
        queryKey: ["offer", offerId],
        queryFn: async () => {
            const { data } = await api.get(`/offers/${offerId}`);
            return data;
        },
        enabled: !!offerId,
    });

    useEffect(() => {
        Fancybox.bind("[data-fancybox='gallery']", {
            Thumbs: {
                autoStart: true,
            },
        });

        return () => {
            Fancybox.unbind("[data-fancybox='gallery']");
        };
    }, [offer]);

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    if (!offerId || !offer) return null;

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
                    <div className="text-center text-gray-600 my-8">Loading offer...</div>
                ) : error ? (
                    <div className="text-center text-red-500 my-8">Failed to load offer details.</div>
                ) : (
                    <>
                        <h2
                            className="text-2xl font-semibold text-[#32492D] text-center mb-2 hover:text-[#273823] transition-all duration-100 ease-in-out transform cursor-pointer"
                            onClick={() => {
                                onClose();
                                onHotelClick?.(offer.hotelIdentifier);
                            }}
                        >
                            {offer.hotelName}
                        </h2>

                        <div className="flex items-center justify-center space-x-1 text-[#32492D] mb-4">
                            {Array.from({ length: offer.stars }).map((_, i) => (
                                <FaStar key={i} size={25} />
                            ))}
                        </div>

                        <div className="flex gap-10 p-4">
                            <div className="relative w-80 h-80 flex-shrink-0 rounded-md overflow-hidden group cursor-pointer">
                                <a href={offer.imagesUrls?.[0]} data-fancybox="gallery">
                                    <img
                                        src={offer.imagesUrls?.[0] ?? "https://via.placeholder.com/200"}
                                        alt={offer.title}
                                        className="w-full h-full object-cover transition duration-300 group-hover:brightness-75"
                                    />
                                    <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-black bg-opacity-30 text-white text-lg font-semibold">
                                        Show more images
                                    </div>
                                </a>

                                {offer.imagesUrls?.slice(1).map((url, idx) => (
                                    <a
                                        key={idx}
                                        href={url}
                                        data-fancybox="gallery"
                                        className="hidden"
                                    >
                                        <img src={url} alt={`Offer image ${idx + 2}`} />
                                    </a>
                                ))}
                            </div>

                            <div className="flex flex-col items-start justify-between">
                                <div className="flex items-start space-x-4">
                                    <h3 className="text-xl font-semibold text-gray-900 text-center">
                                        {offer.title}
                                    </h3>
                                    <div className="flex items-center justify-center text-base bg-[#32492D] text-white rounded-lg px-4">
                                        {offer.rating}
                                    </div>
                                </div>

                                <div className="flex space-x-1 items-center text-[#32492D] mt-2">
                                    <FaLocationDot size={20} />
                                    <div className="text-base text-gray-600">
                                        {offer.location.street}, {offer.location.postalCode} {offer.location.city}, {offer.location.country}
                                    </div>
                                </div>

                                <div className="text-gray-600 text-base mt-2">
                                    {offer.dateFrom} → {offer.dateUntil}
                                </div>

                                <div className="text-gray-600 text-base">
                                    {offer.pricePerNight} €/night
                                </div>

                                <div className="flex flex-wrap gap-2 items-center mt-2">
                                    <div className="flex items-center space-x-1 text-[#32492D]" title={`${offer.roomCount} room(s)`}>
                                        <FaBed size={25} />
                                        <span>{offer.roomCount}</span>
                                    </div>
                                    <div className="flex items-center space-x-1 text-[#32492D]" title={`${offer.peopleCount} people`}>
                                        <FaUserFriends size={25} />
                                        <span>{offer.peopleCount}</span>
                                    </div>
                                    <FacilityIcons facilities={offer.facilities} size={25} />
                                </div>

                                <div className="text-gray-600 mt-2">
                                    {limitText(offer.description, 470)}
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </motion.div>
        </div>
    );
};

export default OfferDetailsModal;