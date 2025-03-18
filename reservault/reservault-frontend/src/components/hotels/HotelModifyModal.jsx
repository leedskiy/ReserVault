import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { FaTimes } from "react-icons/fa";

const HotelModifyModal = ({ hotel, onSubmit, onClose }) => {
    const [isDragging, setIsDragging] = useState(false);
    const [isDirty, setIsDirty] = useState(false);
    const [hotelData, setHotelData] = useState({ ...hotel, images: hotel.images || [] });

    useEffect(() => {
        const handleBeforeUnload = (event) => {
            if (isDirty) {
                event.preventDefault();
                event.returnValue = "";
            }
        };

        window.addEventListener("beforeunload", handleBeforeUnload);
        return () => {
            window.removeEventListener("beforeunload", handleBeforeUnload);
        };
    }, [isDirty]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setIsDirty(true);

        if (["country", "city", "street", "postalCode"].includes(name)) {
            setHotelData((prev) => ({
                ...prev,
                location: { ...prev.location, [name]: value },
            }));
        } else {
            setHotelData((prev) => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!hotelData.id) {
            console.error("Hotel ID is missing.");
            return;
        }

        const formData = new FormData();

        formData.append(
            "hotel",
            JSON.stringify({
                identifier: hotelData.identifier,
                name: hotelData.name,
                description: hotelData.description,
                stars: hotelData.stars,
                location: hotelData.location,
            })
        );

        if (hotelData.images && hotelData.images.length > 0) {
            const hasNewImages = hotelData.images.some((image) => image instanceof File);
            if (hasNewImages) {
                hotelData.images.forEach((image) => {
                    if (image instanceof File) {
                        formData.append("images", image);
                    }
                });
            }
        }

        setIsDirty(false);
        onSubmit(hotelData.id, formData);
    };


    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
            <motion.div
                className="flex flex-col justify-between bg-white rounded-lg shadow-lg p-6 w-full max-w-3xl relative"
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

                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Modify Hotel
                </h2>

                <form onSubmit={handleSubmit} className="space-y-4">

                    <div className="flex space-x-4">
                        <div className="w-1/2">
                            <label className="block text-gray-600">Identifier</label>
                            <input
                                type="text"
                                name="identifier"
                                className="w-full px-4 py-2 bg-gray-200 border rounded-lg"
                                value={hotelData.identifier}
                                disabled
                            />
                        </div>
                        <div className="w-1/2">
                            <label className="block text-gray-600">Hotel Name</label>
                            <input
                                type="text"
                                name="name"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                value={hotelData.name}
                                required
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-gray-600">Description</label>
                        <textarea
                            name="description"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            value={hotelData.description}
                            required
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600">Country</label>
                            <input
                                type="text"
                                name="country"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                value={hotelData.location.country}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">City</label>
                            <input
                                type="text"
                                name="city"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                value={hotelData.location.city}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">Street</label>
                            <input
                                type="text"
                                name="street"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                value={hotelData.location.street}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600">Postal Code</label>
                            <input
                                type="text"
                                name="postalCode"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                value={hotelData.location.postalCode}
                                required
                            />
                        </div>
                    </div>

                    <div className="flex justify-end space-x-4">
                        <button
                            type="button"
                            className="px-4 py-2 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300"
                            onClick={onClose}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="px-6 py-2 text-white bg-[#32492D] hover:bg-[#273823] rounded-lg transition-all duration-300"
                        >
                            Submit
                        </button>
                    </div>

                </form>
            </motion.div>
        </div>
    );
};

export default HotelModifyModal;
