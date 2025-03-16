import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../context/AuthContext";
import { useNavigate, useParams } from "react-router-dom";
import { FaList, FaPlus, FaTimes } from "react-icons/fa";
import { motion } from "framer-motion";
import api from "../api/axios";
import Header from "../components/Header";
import Sidebar from "../components/Sidebar";

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

const HotelList = ({ hotels, isLoading, error }) => {
    const limitText = (text, maxLength) =>
        text.length > maxLength ? text.substring(0, maxLength) + "..." : text;

    return (
        <>
            {isLoading && <div className="text-center text-gray-600">Loading hotels...</div>}
            {error && <div className="text-center text-red-500">Failed to load hotels.</div>}

            <motion.div className="space-y-6 flex flex-col items-center">
                {hotels?.map((hotel, index) => (
                    <motion.div
                        key={hotel.id}
                        className="bg-white shadow-md rounded-lg overflow-hidden flex border border-gray-200 p-4 w-full"
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

                        <div className="w-full px-6 flex flex-col space-y-3">
                            <h2 className="text-lg font-semibold text-gray-900">
                                {limitText(hotel.name, 35)}
                            </h2>
                            <p className="text-sm text-gray-600">
                                {limitText(`${hotel.location.city}, ${hotel.location.country}`, 40)}
                            </p>
                            <p className="text-sm text-gray-700 mt-2">
                                {limitText(hotel.description, 500)}
                            </p>
                        </div>

                        <div className="flex items-center justify-center w-20 h-12">
                            <span className="bg-yellow-700 text-white px-2 py-2 rounded-lg font-bold text-sm">
                                ⭐{hotel.stars}
                            </span>
                        </div>
                    </motion.div>
                ))}
            </motion.div>
        </>
    );
};

const HotelForm = ({ onSubmit, onCancel }) => {
    const [hotelData, setHotelData] = useState({
        identifier: "",
        name: "",
        description: "",
        stars: 5,
        location: { country: "", city: "", street: "", postalCode: "" },
        images: [],
    });

    const [isDragging, setIsDragging] = useState(false);
    const [isDirty, setIsDirty] = useState(false);

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

    const handleFileChange = (e) => {
        setIsDirty(true);
        const files = Array.from(e.target.files);
        setHotelData((prev) => ({
            ...prev,
            images: [...prev.images, ...files],
        }));
    };

    const removeImage = (index) => {
        setIsDirty(true);
        setHotelData((prev) => ({
            ...prev,
            images: prev.images.filter((_, i) => i !== index),
        }));
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleDragLeave = (e) => {
        e.preventDefault();
        setIsDragging(false);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setIsDragging(false);
        setIsDirty(true);

        const files = Array.from(e.dataTransfer.files);
        setHotelData((prev) => ({
            ...prev,
            images: [...prev.images, ...files],
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const formData = new FormData();

        formData.append("hotel", JSON.stringify({
            identifier: hotelData.identifier,
            name: hotelData.name,
            description: hotelData.description,
            stars: hotelData.stars,
            location: hotelData.location,
        }));

        hotelData.images.forEach((image) => {
            formData.append("images", image);
        });

        setIsDirty(false);
        onSubmit(formData);
    };

    return (
        <motion.div
            className="flex items-center justify-center flex flex-col"
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
        >
            <div className="flex flex-col items-center bg-white p-6 rounded-lg shadow-lg w-full">
                <h2 className="text-2xl font-semibold text-gray-900 text-center mb-4">
                    Add Hotel
                </h2>

                <form onSubmit={handleSubmit} className="space-y-4 w-3/4">
                    <div>
                        <label className="block text-gray-600">Identifier</label>
                        <input
                            type="text"
                            name="identifier"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-600">Hotel Name</label>
                        <input
                            type="text"
                            name="name"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-gray-600">Description</label>
                        <textarea
                            name="description"
                            className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                            onChange={handleChange}
                            required
                        />
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

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-gray-600">Country</label>
                            <input
                                type="text"
                                name="country"
                                className="w-full px-4 py-2 border rounded-lg focus:ring focus:ring-blue-300"
                                onChange={handleChange}
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
                                required
                            />
                        </div>
                    </div>

                    <div
                        className={`border-dashed border-2 p-4 flex flex-col items-center cursor-pointer transition-all duration-300 ${isDragging ? "border-blue-500 bg-blue-100" : "border-gray-300 bg-gray-100"
                            }`}
                        onDragOver={handleDragOver}
                        onDragEnter={handleDragOver}
                        onDragLeave={handleDragLeave}
                        onDrop={handleDrop}
                        onClick={() => document.getElementById("fileUpload").click()}
                    >
                        <input
                            type="file"
                            multiple
                            accept="image/*"
                            onChange={handleFileChange}
                            className="hidden"
                            id="fileUpload"
                        />

                        <p className="text-gray-700 font-semibold hover:underline text-center">
                            Drag & Drop or Click to Upload Hotel Images
                        </p>

                        <div className="grid grid-cols-4 gap-2 mt-4">
                            {hotelData.images.map((file, index) => (
                                <div key={index} className="relative">
                                    <img
                                        src={URL.createObjectURL(file)}
                                        className="w-20 h-20 object-cover rounded border"
                                        alt="preview"
                                    />
                                    <button
                                        type="button"
                                        className="absolute top-1 right-1 bg-red-500 text-white text-xs px-1 rounded"
                                        onClick={() => removeImage(index)}
                                    >
                                        <FaTimes />
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="w-full px-4 py-2 text-white bg-gray-900 rounded-lg hover:bg-[#32492D] transition-all duration-300 ease-in-out transform"
                    >
                        Add Hotel
                    </button>

                    <button
                        type="button"
                        className="w-full px-4 py-2 text-gray-700 border rounded-lg hover:bg-gray-200 transition-all duration-300 ease-in-out transform"
                        onClick={onCancel}
                    >
                        Cancel
                    </button>
                </form>
            </div>
        </motion.div>
    );
};

export default AdminHotels;