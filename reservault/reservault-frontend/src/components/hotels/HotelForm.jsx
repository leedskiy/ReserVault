import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { FaTimes } from "react-icons/fa";

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
                        className="w-full px-4 py-2 text-white rounded-lg bg-[#32492D] hover:bg-[#273823] transition-all duration-300 ease-in-out transform"
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

export default HotelForm;