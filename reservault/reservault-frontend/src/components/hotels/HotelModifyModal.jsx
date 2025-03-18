import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { FaTimes } from "react-icons/fa";
import api from "../../api/axios";

const HotelModifyModal = ({ hotel, onSubmit, onClose }) => {
    const [isDirty, setIsDirty] = useState(false);
    const [hotelData, setHotelData] = useState({ ...hotel, images: hotel.images || [] });
    const [draggingIndex, setDraggingIndex] = useState(null);
    const queryClient = useQueryClient();
    const [isDragging, setIsDragging] = useState(false);
    const [newImages, setNewImages] = useState([]);

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

        console.log("Attempting to delete:", imageUrl);

        deleteImageMutation.mutate(imageUrl);
    };

    const deleteImageMutation = useMutation({
        mutationFn: async (imageUrl) => {
            await api.delete(`/admin/hotels/${hotelData.id}/images`, {
                params: { imageUrl },
            });
        },
        onSuccess: (data, imageUrl) => {
            console.log("Image deleted successfully:", imageUrl);

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

    const handleImgAddFileChange = (e) => {
        setIsDirty(true);
        const files = Array.from(e.target.files);
        setNewImages((prev) => [...prev, ...files]);
    };

    const removeImgAddImage = (index) => {
        setIsDirty(true);
        setNewImages((prev) => prev.filter((_, i) => i !== index));
    };

    const handleImgAddDragOver = (e) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleImgAddDragLeave = (e) => {
        e.preventDefault();
        setIsDragging(false);
    };

    const handleImgAddDrop = (e) => {
        e.preventDefault();
        setIsDragging(false);
        setIsDirty(true);

        const files = Array.from(e.dataTransfer.files);
        setNewImages((prev) => [...prev, ...files]);
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
                    onClick={onClose}
                >
                    <FaTimes size={20} />
                </button>

                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Modify Hotel
                </h2>

                <div className="flex space-x-6">
                    <div className="w-1/2 space-y-4">
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
                    </div>

                    <div className="w-1/2">
                        <div>
                            <label className="block text-gray-600">Manage Images</label>
                            <div className="flex space-x-4 overflow-x-auto p-2 border rounded-lg">
                                {hotelData.images.map((img, index) => (
                                    <div
                                        key={img}
                                        draggable
                                        onDragStart={() => handleDragStart(index)}
                                        onDragOver={handleDragOver}
                                        onDrop={() => handleDrop(index)}
                                        className="relative flex flex-col items-center cursor-move"
                                    >
                                        <button
                                            className="absolute top-6 right-0 bg-red-600 text-white rounded-full p-1"
                                            onClick={() => handleRemoveImage(index)}
                                        >
                                            <FaTimes size={14} />
                                        </button>

                                        <span className="text-sm font-bold mb-1">#{index + 1}</span>
                                        <img src={img} alt="Hotel" className="w-24 h-24 rounded-lg shadow" />
                                    </div>
                                ))}
                            </div>
                        </div>
                        <div>
                            <div
                                className={`border-dashed border-2 p-4 flex flex-col items-center cursor-pointer transition-all duration-300 ${isDragging ? "border-blue-500 bg-blue-100" : "border-gray-300 bg-gray-100"
                                    }`}
                                onDragOver={handleImgAddDragOver}
                                onDragEnter={handleImgAddDragOver}
                                onDragLeave={handleImgAddDragLeave}
                                onDrop={handleImgAddDrop}
                                onClick={() => document.getElementById("fileUpload").click()}
                            >
                                <input
                                    type="file"
                                    multiple
                                    accept="image/*"
                                    onChange={handleImgAddFileChange}
                                    className="hidden"
                                    id="fileUpload"
                                />

                                <p className="text-gray-700 font-semibold hover:underline text-center">
                                    Drag & Drop or Click to Upload Hotel Images
                                </p>

                                <div className="grid grid-cols-4 gap-2 mt-4">
                                    {newImages.map((file, index) => (
                                        <div key={index} className="relative">
                                            <img
                                                src={file instanceof File ? URL.createObjectURL(file) : file}
                                                alt="Hotel"
                                                className="w-24 h-24 rounded-lg shadow"
                                            />
                                            <button
                                                type="button"
                                                className="absolute top-1 right-1 bg-red-500 text-white text-xs px-1 rounded"
                                                onClick={() => removeImgAddImage(index)}
                                            >
                                                <FaTimes />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                        <div>
                            <label className="block text-gray-600">Stars</label>
                            <select
                                name="stars"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
                                required
                            >
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <option key={star} value={star}>{star} Stars</option>
                                ))}
                            </select>
                        </div>
                    </div>
                </div>

                <div className="flex justify-end space-x-4 mt-6">
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
