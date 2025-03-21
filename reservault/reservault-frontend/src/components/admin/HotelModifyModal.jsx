import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { FaTimes } from "react-icons/fa";
import api from "../../api/axios";
import HotelStars from "./HotelStars";
import ImageUploader from "../common/ImageUploader";

const HotelModifyModal = ({ hotel, onSubmit, onClose }) => {
    const [isDirty, setIsDirty] = useState(false);
    const [hotelData, setHotelData] = useState({ ...hotel, images: hotel.images || [] });
    const [draggingIndex, setDraggingIndex] = useState(null);
    const queryClient = useQueryClient();
    const [isDragging, setIsDragging] = useState(false);
    const [newImages, setNewImages] = useState([]);
    const [imagesToDelete, setImagesToDelete] = useState([]);

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
                imagesUrls: hotelData.images
            })
        );

        if (newImages.length > 0) {
            newImages.forEach((image) => {
                formData.append("images", image);
            });
        }

        if (imagesToDelete.length > 0) {
            for (const imageUrl of imagesToDelete) {
                await deleteImageMutation.mutateAsync(imageUrl);
            }
        }

        setIsDirty(false);
        onSubmit(hotelData.id, formData);
    };

    const handleDragStart = (index) => {
        setDraggingIndex(index);
    };

    const handleDragOver = (event) => {
        event.preventDefault();
    };

    const handleDrop = (index) => {
        if (draggingIndex === null || draggingIndex === index) return;

        const updatedImages = [...hotelData.images];
        const draggedImage = updatedImages.splice(draggingIndex, 1)[0];
        updatedImages.splice(index, 0, draggedImage);

        setHotelData((prev) => ({
            ...prev,
            images: updatedImages,
        }));

        setDraggingIndex(null);
        setIsDirty(true);
    };

    const handleRemoveImage = (index) => {
        const imageUrl = hotelData.images[index];

        setImagesToDelete((prev) => [...prev, imageUrl]);

        setHotelData((prev) => ({
            ...prev,
            images: prev.images.filter((img, i) => i !== index),
        }));
    };

    const deleteImageMutation = useMutation({
        mutationFn: async (imageUrl) => {
            await api.delete(`/admin/hotels/${hotelData.id}/images`, {
                params: { imageUrl },
            });
        },
        onSuccess: (data, imageUrl) => {
            setHotelData((prev) => ({
                ...prev,
                images: prev.images.filter((img) => img !== imageUrl),
            }));

            queryClient.invalidateQueries(["admin", "hotels"]);
        },
        onError: (error) => {
            console.error("Failed to delete image:", error.response?.data || error.message);
        },
    });

    const handleClose = () => {
        setImagesToDelete([]);
        onClose();
    };

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
                    onClick={handleClose}
                >
                    <FaTimes size={20} />
                </button>

                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Modify Hotel
                </h2>

                <div className="flex gap-10 h-full p-4">
                    <div className="flex flex-col w-1/2 space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-gray-600">Identifier</label>
                                <input
                                    type="text"
                                    name="identifier"
                                    className="w-full px-4 py-2 bg-gray-200 border rounded-lg"
                                    value={hotelData.identifier}
                                    disabled
                                />
                            </div>
                            <div>
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
                    </div>

                    <div className="flex flex-col flex-grow max-w-[40%] space-y-6">
                        <div>
                            <label className="block text-gray-600">Manage Images</label>
                            <div className="flex gap-x-4 overflow-x-auto p-2 border rounded-lg whitespace-nowrap">
                                {hotelData.images.map((img, index) => (
                                    <div
                                        key={img}
                                        draggable
                                        onDragStart={() => handleDragStart(index)}
                                        onDragOver={handleDragOver}
                                        onDrop={() => handleDrop(index)}
                                        className="relative inline-block cursor-move"
                                    >
                                        {hotelData.images.length > 1 && (
                                            <button
                                                className="absolute top-6 right-0 bg-red-600 text-white rounded-full p-1"
                                                onClick={() => handleRemoveImage(index)}
                                            >
                                                <FaTimes size={14} />
                                            </button>
                                        )}

                                        <span className="text-sm font-bold mb-1 block text-center">#{index + 1}</span>
                                        <img
                                            src={img}
                                            alt="Hotel"
                                            className="w-24 h-24 rounded-lg shadow object-cover min-w-[96px] min-h-[96px]"
                                        />
                                    </div>
                                ))}
                            </div>
                        </div>

                        <ImageUploader
                            images={newImages}
                            setImages={setNewImages}
                            isDragging={isDragging}
                            setIsDragging={setIsDragging}
                        />
                    </div>

                    <HotelStars hotelData={hotelData} setHotelData={setHotelData} />
                </div>


                <div className="flex justify-end space-x-4 mt-6">
                    <button
                        type="button"
                        className="px-8 py-2 min-w-32 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300"
                        onClick={handleClose}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="px-8 py-2 min-w-32 text-white bg-[#32492D] hover:bg-[#273823] rounded-lg transition-all duration-300"
                        onClick={handleSubmit}
                    >
                        Submit
                    </button>
                </div>
            </motion.div>
        </div>
    );
};

export default HotelModifyModal;