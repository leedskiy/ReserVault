import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import api from "../../api/axios";
import HotelStars from "../common/HotelStars";
import ModifyFormContainer from "../common/ModifyFormContainer";
import { div } from "framer-motion/client";

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
        <ModifyFormContainer
            title="Modify Hotel"
            onClose={handleClose}
            onSubmit={handleSubmit}
            images={hotelData.images}
            handleDragStart={handleDragStart}
            handleDragOver={handleDragOver}
            handleDrop={handleDrop}
            handleRemoveImage={handleRemoveImage}
            newImages={newImages}
            setNewImages={setNewImages}
            isDragging={isDragging}
            setIsDragging={setIsDragging}
            isDirty={isDirty}
            setImagesToDelete={setImagesToDelete}
            itemsName="Hotel"
            leftContent={(
                <div className="flex flex-col w-1/2 space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600 font-medium mb-1">Identifier</label>
                            <input
                                type="text"
                                name="identifier"
                                className="w-full px-4 py-2 bg-gray-200 border rounded-lg"
                                value={hotelData.identifier}
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600 font-medium mb-1">Hotel Name</label>
                            <input
                                type="text"
                                name="name"
                                className="w-full border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                onChange={handleChange}
                                value={hotelData.name}
                                required
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-gray-600 font-medium mb-1">Description</label>
                        <textarea
                            name="description"
                            className="w-full border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                            onChange={handleChange}
                            value={hotelData.description}
                            required
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600 font-medium mb-1">Country</label>
                            <input
                                type="text"
                                name="country"
                                className="w-full border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                onChange={handleChange}
                                value={hotelData.location.country}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600 font-medium mb-1">City</label>
                            <input
                                type="text"
                                name="city"
                                className="w-full border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                onChange={handleChange}
                                value={hotelData.location.city}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600 font-medium mb-1">Street</label>
                            <input
                                type="text"
                                name="street"
                                className="w-full border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                onChange={handleChange}
                                value={hotelData.location.street}
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600 font-medium mb-1">Postal Code</label>
                            <input
                                type="text"
                                name="postalCode"
                                className="w-full border border-gray-300 rounded-md px-3 py-2
                                        focus:outline-none transition-all duration-100 ease-in-out transform
                                        rounded-lg focus:outline-none focus:border-[#32492D] focus:ring-1 focus:ring-[#32492D]"
                                onChange={handleChange}
                                value={hotelData.location.postalCode}
                                required
                            />
                        </div>
                    </div>
                </div>
            )}
            rightContent={(
                <div className="h-auto flex flex-col">
                    <label className="block text-gray-600 font-medium mb-1">Stars</label>
                    <HotelStars
                        hotelData={hotelData}
                        setHotelData={setHotelData}
                    />
                </div >
            )}
        />
    );

};

export default HotelModifyModal;