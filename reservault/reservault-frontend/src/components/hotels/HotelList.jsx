import { motion } from "framer-motion";
import { FaStar, FaEllipsisH, FaEdit, FaTrash } from "react-icons/fa";
import { useState, useRef, useEffect } from "react";
import DropdownMenu from "../DropdownMenu";

const HotelList = ({ hotels, isLoading, error, onModify, onDelete }) => {
    const [openDropdown, setOpenDropdown] = useState(null);
    const dropdownRefs = useRef([]);

    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    const handleToggleDropdown = (hotelId) => {
        setOpenDropdown((prev) => (prev === hotelId ? null : hotelId));
    };

    const handleClickOutside = (event) => {
        if (
            dropdownRefs.current.length &&
            !dropdownRefs.current.some((ref) => ref?.contains(event.target))
        ) {
            setOpenDropdown(null);
        }
    };

    useEffect(() => {
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <>
            {isLoading && <div className="text-center text-gray-600">Loading hotels...</div>}
            {error && <div className="text-center text-red-500">Failed to load hotels.</div>}

            <motion.div className="space-y-6 flex flex-col items-center w-full">
                {hotels?.map((hotel, index) => (
                    <motion.div
                        key={hotel.id}
                        className="bg-white shadow-md rounded-lg flex border border-gray-200 p-4 w-full relative"
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

                        <div className="w-full px-6 flex flex-col">
                            <h2 className="text-lg font-semibold text-gray-900">
                                {limitText(hotel.name, 35)}
                            </h2>

                            <p className="text-sm text-gray-600">
                                {limitText(`${hotel.location.city}, ${hotel.location.country}`, 40)}
                            </p>

                            <div className="flex items-center space-x-1 text-yellow-500 mt-2">
                                {Array.from({ length: hotel.stars }).map((_, i) => (
                                    <FaStar key={i} />
                                ))}
                            </div>

                            <p className="text-sm text-gray-700 mt-4">
                                {limitText(hotel.description, 500)}
                            </p>
                        </div>

                        <div className="relative flex">
                            <button
                                onClick={() => handleToggleDropdown(hotel.id)}
                                ref={(el) => (dropdownRefs.current[index] = el)}
                                className="flex items-center justify-center w-8 h-8 rounded-lg text-[#32492D] hover:text-[#273823] hover:bg-gray-200 flex-shrink-0"
                            >
                                <FaEllipsisH size={20} />
                            </button>

                            {openDropdown === hotel.id && (
                                <DropdownMenu
                                    isOpen={openDropdown === hotel.id}
                                    onClose={() => setOpenDropdown(null)}
                                    menuItems={[
                                        {
                                            label: "Modify",
                                            icon: FaEdit,
                                            onClick: () => onModify(hotel.id)
                                        },
                                        {
                                            label: "Delete",
                                            icon: FaTrash,
                                            onClick: () => onDelete(hotel.id)
                                        },
                                    ]}
                                    position="left-0 top-10"
                                />
                            )}
                        </div>
                    </motion.div>
                ))}
            </motion.div>
        </>
    );
};

export default HotelList;
